package fs.playground.optimisticlock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import javax.persistence.EntityManager
import kotlin.streams.toList

interface TicketRepository : CrudRepository<Ticket, String>

interface TicketEventRepository : CrudRepository<TicketEvent, Long>, TicketEventRepositoryExt {
}

interface TicketEventRepositoryExt {
    fun findByTicketNameWithEventType(ticketName: String, ticketEventType: TicketEventType): List<TicketEvent>
    fun findByIdGreaterThanAndTicketNameWithEventType(id: Long, ticketName: String, ticketEventType: TicketEventType): List<TicketEvent>
}

class TicketEventRepositoryImpl(
        @Autowired val entityManager: EntityManager
) : TicketEventRepositoryExt {

    override fun findByTicketNameWithEventType(ticketName: String, ticketEventType: TicketEventType): List<TicketEvent> {
        val query = entityManager.createQuery("select te from TicketEvent te where ticketName = :ticketName", TicketEvent::class.java)
        query.setParameter("ticketName", ticketName)
        return query.resultList.stream().filter { it.eventType == ticketEventType }.toList()
    }

    override fun findByIdGreaterThanAndTicketNameWithEventType(id: Long, ticketName: String, ticketEventType: TicketEventType): List<TicketEvent> {
        val query = entityManager.createQuery("select te from TicketEvent te where ticketName = :ticketName and id > :id", TicketEvent::class.java)
        query.setParameter("ticketName", ticketName)
        query.setParameter("id", id)
        return query.resultList.stream().filter { it.eventType == ticketEventType }.toList()
    }
}

interface SnapshotRepository : CrudRepository<Snapshot, SnapshotId> {
    fun findFirst1ByIdTicketNameOrderByIdEventIdDesc(ticketName: String): Snapshot?
}