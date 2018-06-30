package fs.playground.optimisticlock

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.LogManager
import org.hibernate.StaleObjectStateException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.auditing.AuditingHandler
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

enum class TicketEventType {
    RESERVATION, CANCEL
}

enum class ReservationType {
    OVER, DONE, FIAL
}

@Component
class TicketService(
        private @Autowired val ticketRepository: TicketRepository,
        private @Autowired val ticketEventRepository: TicketEventRepository,
        private @Autowired val auditingHandler: AuditingHandler
) {

    private val logger = LogManager.getLogger(TicketService::class.java)

    private var mapper: ObjectMapper = ObjectMapper()

    fun findTicket(ticketName: String): Optional<Ticket> = ticketRepository.findById(ticketName)

    fun findTicketAll(): MutableIterable<Ticket> = ticketRepository.findAll()

    fun creatTicket(name: String, maximum: Int) {
        logger.info("생성: ${name}, ${maximum}")
        ticketRepository.save(Ticket(name, maximum, LocalDateTime.now()))
    }

    fun reservationTicket(ticketName: String, retry: Int): ReservationType {
        if (retry > 10) {
            return ReservationType.FIAL
        }
        val usedTicketCount = ticketEventRepository.findByTicketName(ticketName).filter {
            it.eventType == TicketEventType.RESERVATION
        }.count()
        val mayBeTicket = ticketRepository.findById(ticketName)
        if (!mayBeTicket.isPresent()) {
            throw Exception("티켓없음 ${ticketName}")
        }
        val ticket = mayBeTicket.get()
        if (ticket.maxium < usedTicketCount) {
            logger.warn("수량초과: ${usedTicketCount}")
            return ReservationType.OVER
        }
        try {
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
                            "maximum" to ticket.maxium,
                            "used" to usedTicketCount,
                            "updated" to ticket.updated.format(DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss")),
                            "version" to ticket.version
                    ))
            ))
            return ReservationType.DONE
        } catch (e: Exception) {
            when (e) {
                is ObjectOptimisticLockingFailureException, is StaleObjectStateException -> {
                    logger.info("락걸림")
                    return reservationTicket(ticketName, retry + 1)
                }
                else -> throw e
            }
        }
    }
}