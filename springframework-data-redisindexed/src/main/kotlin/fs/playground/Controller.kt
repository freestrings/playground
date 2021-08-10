package fs.playground

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(val personRepository: PersonRepository, val personsRepository: PersonsRepository) {

    @GetMapping("/load")
    fun load() {
        var id = 0
        for (i in 1 until 10) {
            id = id.inc()
            personRepository.save(Person(stat = "1", cstno = i.toLong(), pid = id.toLong()))
            id = id.inc()
            personRepository.save(Person(stat = "2", cstno = i.toLong(), pid = id.toLong()))
            id = id.inc()
            personRepository.save(Person(stat = "2", cstno = i.toLong(), pid = id.toLong()))
            id = id.inc()
            personRepository.save(Person(stat = "3", cstno = i.toLong(), pid = id.toLong()))
            id = id.inc()
            personRepository.save(Person(stat = "3", cstno = i.toLong(), pid = id.toLong()))
            id = id.inc()
            personRepository.save(Person(stat = "3", cstno = i.toLong(), pid = id.toLong()))
            id = id.inc()
            personRepository.save(Person(stat = "4", cstno = i.toLong(), pid = id.toLong()))
            id = id.inc()
            personRepository.save(Person(stat = "4", cstno = i.toLong(), pid = id.toLong()))
            id = id.inc()
            personRepository.save(Person(stat = "4", cstno = i.toLong(), pid = id.toLong()))
            id = id.inc()
            personRepository.save(Person(stat = "4", cstno = i.toLong(), pid = id.toLong()))
        }

        for (i in 1 until 10) {
            personsRepository.save(
                Persons(
                    cstno = i.toLong(),
                    c1 = InnerPerson(stat = "1", cstno = i.toLong()),
                    c2 = InnerPerson(stat = "2", cstno = i.toLong()),
                    c3 = InnerPerson(stat = "2", cstno = i.toLong()),
                    c4 = InnerPerson(stat = "3", cstno = i.toLong()),
                    c5 = InnerPerson(stat = "3", cstno = i.toLong()),
                    c6 = InnerPerson(stat = "3", cstno = i.toLong()),
                    c7 = InnerPerson(stat = "4", cstno = i.toLong()),
                    c8 = InnerPerson(stat = "4", cstno = i.toLong()),
                    c9 = InnerPerson(stat = "4", cstno = i.toLong()),
                    c0 = InnerPerson(stat = "4", cstno = i.toLong()),
                )
            )
        }
    }

    @GetMapping("/person/{cstno}")
    fun findAllPersonByPid(@PathVariable cstno: Long, @RequestParam stat: String): List<Person> {
        return personRepository.findAllByCstno(cstno).filter { it.stat == stat }.toList()
    }

    @GetMapping("/persons/{cstno}")
    fun findAllPersonsByPid(@PathVariable cstno: Long, @RequestParam stat: String): List<Person> {
        val persons = personsRepository.findById(cstno).get()
        val list = mutableListOf<Person>()
        persons.c1?.let {
            if (it.stat == stat) {
                list.add(Person(stat = stat, cstno = it.cstno, pid = 0))
            }
        }
        persons.c2?.let {
            if (it.stat == stat) {
                list.add(Person(stat = stat, cstno = it.cstno, pid = 0))
            }
        }
        persons.c3?.let {
            if (it.stat == stat) {
                list.add(Person(stat = stat, cstno = it.cstno, pid = 0))
            }
        }
        persons.c4?.let {
            if (it.stat == stat) {
                list.add(Person(stat = stat, cstno = it.cstno, pid = 0))
            }
        }
        persons.c5?.let {
            if (it.stat == stat) {
                list.add(Person(stat = stat, cstno = it.cstno, pid = 0))
            }
        }
        persons.c6?.let {
            if (it.stat == stat) {
                list.add(Person(stat = stat, cstno = it.cstno, pid = 0))
            }
        }
        persons.c7?.let {
            if (it.stat == stat) {
                list.add(Person(stat = stat, cstno = it.cstno, pid = 0))
            }
        }
        persons.c8?.let {
            if (it.stat == stat) {
                list.add(Person(stat = stat, cstno = it.cstno, pid = 0))
            }
        }
        persons.c9?.let {
            if (it.stat == stat) {
                list.add(Person(stat = stat, cstno = it.cstno, pid = 0))
            }
        }
        persons.c0?.let {
            if (it.stat == stat) {
                list.add(Person(stat = stat, cstno = it.cstno, pid = 0))
            }
        }
        return list
    }
}