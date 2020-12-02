package fs.playground

import fs.playground.FsDispatcher.asAsync
import fs.playground.FsDispatcher.withSlave
import fs.playground.entity.Person
import fs.playground.repository.PersonRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class SpringframeworkWebfluxJpaApplication

fun main(args: Array<String>) {
    runApplication<SpringframeworkWebfluxJpaApplication>(*args)
}

@RestController
class Ctrl(val personService: PersonService, val newPersionService: NewPersonService, val personRepository: PersonRepository) {

    @GetMapping("/complex/readonly")
    suspend fun complexReadOnlyWithWrite() = personService.complexWithReadOnly(UUID.randomUUID().toString())

    @GetMapping("/complex")
    suspend fun complexWithWrite(@RequestParam("id") id: String) = personService.complex(id)

    @GetMapping("/master/read")
    suspend fun masterRead() = personService.read("master - ${UUID.randomUUID()}")

    @GetMapping("/master/write")
    @Transactional
    suspend fun masterWrite() : Person {
        println(Thread.currentThread())
        return personRepository.save(Person(name = "1"))
    }

    @GetMapping("/slave/read")
    suspend fun readFromSlave() = personService.readFromSlave("slave - ${UUID.randomUUID()}")

    @GetMapping("/slave/write")
    suspend fun writeToSlave() = personService.writeToSlave(UUID.randomUUID().toString())

    @GetMapping("/slave/write2")
    suspend fun writeToSlaveAsReadonl2() = withSlave { personService.write(UUID.randomUUID().toString()) }

    @GetMapping("/slave/readall")
    suspend fun readAllFromSlave() = personService.readAllFromSlave("slaveall - ${UUID.randomUUID()}")

    @GetMapping("/slave/read/async")
    suspend fun readFromSlaveAsyc(): Long {
        val r = personService.readFromSlaveAsync(UUID.randomUUID().toString())
        return r.await()
    }

    @ExperimentalTime
    @GetMapping("/bench/async")
    suspend fun benchAsync() = measureTime {
        withSlave {
            val r1 = asAsync {
                debugPrint("1.")
                Thread.sleep(3000)
                debugPrint("2.")
                personService.personRepository.countByName("slave1")
            }
            val r2 = asAsync {
                debugPrint("1.1")
                Thread.sleep(3000)
                debugPrint("1.2")
                personService.personRepository.countByName("slave2")
            }
            awaitAll(r1, r2)
            personService.personRepository.countByName("slave3")
        }
        debugPrint("3.")
        personService.personRepository.countByName("master4")
    }.toInt(DurationUnit.MILLISECONDS)

    @GetMapping("/new/async")
    suspend fun newAsync() {
        val a = asAsync {
            debugPrint("1")

            withSlave {
                debugPrint("2")

                val a = asAsync {
                    Thread.sleep(1000)
                    debugPrint("3")
                }

                val b = asAsync {
                    Thread.sleep(1000)
                    debugPrint("3-1")
                }

                debugPrint("2-2")
                awaitAll(a, b)
            }

            debugPrint("1-1")
        }
        a.await()

        debugPrint("0-1")
    }

    @GetMapping("/new/async/write")
    suspend fun newAsyncWrite(): String {
        val a = asAsync {
            newPersionService.write(UUID.randomUUID().toString())
        }
        return a.await().name
    }

    @GetMapping("/new/async/write/readonly/with_error")
    suspend fun newAsyncWriteReadOnlyWithError(): String {
        val a = asAsync {
            withSlave {
                debugPrint("1")
                newPersionService.write(UUID.randomUUID().toString())
            }
        }
        return a.await().name
    }

    @GetMapping("/new/async/readonly")
    suspend fun newAsyncWriteReadOnly() = withSlave {
        val a = asAsync {
            withSlave {
                debugPrint("1")
                newPersionService.read("s-${UUID.randomUUID()}")
            }
            debugPrint("2")
            newPersionService.read("m1-${UUID.randomUUID()}")
        }
        debugPrint("3")
        newPersionService.read("m2-${UUID.randomUUID()}")
        a.await()
    }

    @GetMapping("/new/simple")
    suspend fun newAsyncSimple() = newPersionService.read("${UUID.randomUUID()}")

    @GetMapping("/new/default")
    suspend fun newDefault() = newPersionService.write("${UUID.randomUUID()}")

}

@Service
class NewPersonService(val personRepository: PersonRepository) {

    @Transactional
    suspend fun write(uuid: String) = personRepository.save(Person(name = uuid))

    suspend fun read(uuid: String) = personRepository.countByName(uuid)
}

@Service
class PersonService(val personRepository: PersonRepository) {

    @Autowired
    lateinit var _self: PersonService

    suspend fun read(uuid: String) = personRepository.countByName(uuid)

    suspend fun readAsync(uuid: String): Deferred<Long> {
        return asAsync {
            personRepository.countByName(uuid)
        }
    }

    suspend fun readFromSlave(uuid: String) = withSlave { read(uuid) }

    suspend fun readFromSlaveAsync(uuid: String) = withSlave { readAsync(uuid) }

    @Transactional
    suspend fun write(uuid: String) = personRepository.save(Person(id = uuid))

    @Transactional
    suspend fun writeToSlave(uuid: String) = withSlave {
        personRepository.save(Person(name = uuid))
    }

    suspend fun readAllFromSlave(uuid: String): Long {
        return withSlave {
            val c1 = readAsync(uuid)
            val c2 = readAsync(uuid)
            val c3 = readFromSlaveAsync(uuid)
            val (r1, r2, r3) = awaitAll(c1, c2, c3)
            r1 + r2 + r3
        }
    }

    suspend fun complexWithReadOnly(uuid: String): Long {
        return withSlave {
            _self.write(uuid)
            val c1 = readAsync(uuid)
            val c2 = readAsync(uuid)
            val c3 = readFromSlaveAsync(uuid)
            _self.write(uuid)
            val (r1, r2, r3) = awaitAll(c1, c2, c3)
            r1 + r2 + r3
        }
    }

    @Transactional
    suspend fun complex(uuid: String): Long {
        println(Thread.currentThread())
        personRepository.save(Person(id = uuid))
//        _self.write("mw1-$uuid")
//        val c1 = readAsync("m1-$uuid")
//        val c2 = readAsync("m2-$uuid")
//        val c3 = readFromSlaveAsync("s-$uuid")
//        _self.write("mw2-$uuid")
//        val (r1, r2, r3) = awaitAll(c1, c2, c3)
//        return r1 + r2 + r3
        return 0
    }
}
