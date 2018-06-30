package fs.playground.optimisticlock

data class Reservation(val ticketName: String)

data class TicketCreate(val name: String, val maxium: Int)