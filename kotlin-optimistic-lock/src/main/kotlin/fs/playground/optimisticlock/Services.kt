package fs.playground.optimisticlock

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.LogManager
import org.hibernate.StaleObjectStateException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.auditing.AuditingHandler
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture


enum class TicketEventType {
    RESERVATION, CANCEL
}

enum class ConsumeType {
    OVER, DONE, RETRY
}

@Component
class TicketService(
        @Autowired private val ticketRepository: TicketRepository,
        @Autowired private val ticketEventRepository: TicketEventRepository,
        @Autowired private val auditingHandler: AuditingHandler,
        @Autowired private val trasactionManager: JpaTransactionManager
) {

    private val logger = LogManager.getLogger(TicketService::class.java)

    private var mapper: ObjectMapper = ObjectMapper()

    fun create(name: String, maximum: Int): CompletableFuture<Ticket>? {
        return CompletableFuture.supplyAsync {
            logger.info("생성: ${name}, ${maximum}")
            ticketRepository.save(Ticket(name, maximum, LocalDateTime.now()))
        }
    }

    fun usedTicketCount(ticketName: String) = ticketEventRepository.findByTicketName(ticketName)
            .stream()
            .filter { it.eventType == TicketEventType.RESERVATION }
            .count()

    fun find(ticketName: String): Ticket {
        val mayBeTicket = ticketRepository.findById(ticketName)
        if (!mayBeTicket.isPresent()) {
            throw Exception("존재하지 않는 티켓: ${ticketName}")
        }
        return mayBeTicket.get()
    }

    fun consumAsSync(ticketName: String, uuid: String): ConsumeType {
        val usedTicketCount = usedTicketCount(ticketName)
        val ticket = find(ticketName)
        if (ticket.max< usedTicketCount) {
            logger.info("수량초과: ${uuid} - ${usedTicketCount}")
            return ConsumeType.OVER
        }
        val txDef = DefaultTransactionDefinition()
        val txStatus = trasactionManager.getTransaction(txDef)
        return try {
            ticket.updated = LocalDateTime.now()
            auditingHandler.markModified(ticket)
            ticketRepository.save(ticket)
            ticketEventRepository.save(TicketEvent(
                    eventType = TicketEventType.RESERVATION,
                    ticketName = ticketName,
                    //
                    // TODO Jackson LocalDateTime 포멧적용
                    //
                    payload = mapper.writeValueAsString(hashMapOf(
                            "name" to ticket.name,
                            "maximum" to ticket.max,
                            "used" to usedTicketCount + 1,
                            "updated" to ticket.updated.format(DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss")),
                            "version" to ticket.version
                    ))
            ))
            trasactionManager.commit(txStatus)
            ConsumeType.DONE
        } catch (e: Exception) {
            if (!txStatus.isCompleted) {
                trasactionManager.rollback(txStatus)
            }
            when (e) {
                is ObjectOptimisticLockingFailureException, is StaleObjectStateException -> {
                    logger.info("락걸림: ${uuid}")
                    ConsumeType.RETRY
                }
                else -> {
                    throw e
                }
            }
        }
    }

    fun consumWithRetry(ticketName: String, uuid: String): CompletableFuture<ConsumeType> {
        val result = CompletableFuture<ConsumeType>()
        consumWithRetry(ticketName, result, 10, uuid)
        return result
    }

    private fun consumWithRetry(ticketName: String, result: CompletableFuture<ConsumeType>, retry: Int, uuid: String) {
        logger.info("#UUID-1: ${uuid} - ${retry}")
        CompletableFuture.supplyAsync { consumAsSync(ticketName, uuid) }.thenApply {
            logger.info("#UUID-2: ${uuid} - ${it}")
            when (it) {
                ConsumeType.RETRY -> {
                    if (retry > 0) {
                        consumWithRetry(ticketName, result, retry - 1, uuid)
                    } else {
                        result.complete(ConsumeType.RETRY)
                    }
                }
                else -> {
                    result.complete(it)
                }
            }
        }.exceptionally {
            logger.info("#UUID-3: ${uuid} - ${it.message}")
            result.completeExceptionally(it)
        }
    }

}