package fs.playground

import fs.playground.FsDispatcher.asFsContext
import fs.playground.FsDispatcher.asNewAsync
import fs.playground.FsDispatcher.asNewReadOnly
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
class Ctrl(val personService: PersonService, val newPersionService: NewPersonService) {

    @GetMapping("/complex/readonly")
    suspend fun complexReadOnlyWithWrite() = asFsContext {
        personService.complexWithReadOnly(UUID.randomUUID().toString())
    }

    @GetMapping("/complex")
    suspend fun complexWithWrite(@RequestParam("id") id: String) = asFsContext {
        personService.complex(id)
    }

    @GetMapping("/master/read")
    suspend fun masterRead() = asFsContext {
        personService.read("master - ${UUID.randomUUID()}")
    }

    @GetMapping("/master/write")
    suspend fun masterWrite() = asFsContext {
        personService.write(UUID.randomUUID().toString())
    }

    @GetMapping("/slave/read")
    suspend fun readFromSlave() = asFsContext {
        personService.readFromSlave("slave - ${UUID.randomUUID()}")
    }

    @GetMapping("/slave/write")
    suspend fun writeToSlave() = asFsContext {
        personService.writeToSlave(UUID.randomUUID().toString())
    }

    @GetMapping("/slave/write2")
    suspend fun writeToSlaveAsReadonl2() = asFsContext {
        asNewReadOnly { personService.write(UUID.randomUUID().toString()) }
    }

    @GetMapping("/slave/readall")
    suspend fun readAllFromSlave() = asFsContext {
        personService.readAllFromSlave("slaveall - ${UUID.randomUUID()}")
    }

    @GetMapping("/slave/read/async")
    suspend fun readFromSlaveAsyc() = asFsContext {
        val r = personService.readFromSlaveAsync(UUID.randomUUID().toString())
        r.await()
    }

    @ExperimentalTime
    @GetMapping("/bench/async")
    suspend fun benchAsync() = asFsContext {
        measureTime {
            asNewReadOnly {
                val r1 = asNewAsync {
                    debugPrint("1.")
                    Thread.sleep(3000)
                    debugPrint("2.")
                    personService.personRepository.countByName("slave1")
                }
                val r2 = asNewAsync {
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
    }

    @GetMapping("/new/async")
    suspend fun newAsync() = asFsContext {
        val a = asNewAsync {
            debugPrint("1")

            asNewReadOnly {
                debugPrint("2")

                val a = asNewAsync {
                    Thread.sleep(1000)
                    debugPrint("3")
                }

                val b = asNewAsync {
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
    suspend fun newAsyncWrite() = asFsContext {
        val a = asNewAsync {
            newPersionService.write(UUID.randomUUID().toString())
        }
        a.await()
    }

    @GetMapping("/new/async/write/readonly/with_error")
    suspend fun newAsyncWriteReadOnlyWithError() = asFsContext {
        val a = asNewAsync {
            asNewReadOnly {
                debugPrint("1")
                newPersionService.write(UUID.randomUUID().toString())
            }
        }
        a.await()
    }

    @GetMapping("/new/async/readonly")
    suspend fun newAsyncWriteReadOnly() = asFsContext {
        asNewReadOnly {
            val a = asNewAsync {
                asNewReadOnly {
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
    }

    @GetMapping("/new/simple")
    suspend fun newAsyncSimple() = asFsContext {
        newPersionService.read("${UUID.randomUUID()}")
    }

    @GetMapping("/new/default")
    suspend fun newDefault() = asFsContext { newPersionService.write("${UUID.randomUUID()}") }

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
        return asNewAsync {
            personRepository.countByName(uuid)
        }
    }

    suspend fun readFromSlave(uuid: String) = asNewReadOnly { read(uuid) }

    suspend fun readFromSlaveAsync(uuid: String) = asNewReadOnly { readAsync(uuid) }

    @Transactional
    suspend fun write(uuid: String) = personRepository.save(Person(name = uuid))

    @Transactional
    suspend fun writeToSlave(uuid: String) = asNewReadOnly {
        personRepository.save(Person(name = uuid))
    }

    suspend fun readAllFromSlave(uuid: String): Long {
        return asNewReadOnly {
            val c1 = readAsync(uuid)
            val c2 = readAsync(uuid)
            val c3 = readFromSlaveAsync(uuid)
            val (r1, r2, r3) = awaitAll(c1, c2, c3)
            r1 + r2 + r3
        }
    }

    suspend fun complexWithReadOnly(uuid: String): Long {
        return asNewReadOnly {
            _self.write(uuid)
            val c1 = readAsync(uuid)
            val c2 = readAsync(uuid)
            val c3 = readFromSlaveAsync(uuid)
            _self.write(uuid)
            val (r1, r2, r3) = awaitAll(c1, c2, c3)
            r1 + r2 + r3
        }
    }

    suspend fun complex(uuid: String) {
        _self.write("mw1-$uuid")
        val c1 = readAsync("m1-$uuid")
        val c2 = readAsync("m2-$uuid")
        val c3 = readFromSlaveAsync("s-$uuid")
        _self.write("mw2-$uuid")
        val (r1, r2, r3) = awaitAll(c1, c2, c3)
        r1 + r2 + r3
    }
}
