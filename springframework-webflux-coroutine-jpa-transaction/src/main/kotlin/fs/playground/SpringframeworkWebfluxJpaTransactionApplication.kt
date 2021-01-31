package fs.playground

import fs.playground.FsDispatcher.asAsync
import fs.playground.FsDispatcher.getAsyncFsContext
import fs.playground.FsDispatcher.getContext
import fs.playground.FsDispatcher.putContext
import fs.playground.FsDispatcher.withSlave
import fs.playground.entity.Person
import fs.playground.repository.PersonRepository
import kotlinx.coroutines.Deferred
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.transaction.Transactional
import kotlin.coroutines.coroutineContext

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class SpringframeworkWebfluxJpaApplication

fun main(args: Array<String>) {
    runApplication<SpringframeworkWebfluxJpaApplication>(*args)
}

@RestController
class Ctrl(val personService: PersonService) {
    @GetMapping("/async")
    suspend fun async(@RequestParam(value = "value") param: String): Long {
        return asyncCall(param)
    }

    @GetMapping("/asyncAb")
    suspend fun asyncAb(): Long {
        val param = getAsyncFsContext(coroutineContext).uuid
        return asyncCall(param)
    }

    @GetMapping("/syncAb")
    suspend fun syncAb(): Long {
        val param = getAsyncFsContext(coroutineContext).uuid
        return syncCall(param)
    }

    @GetMapping("/asyncSlave")
    suspend fun asyncSlave(@RequestParam(value = "value") param: String): Long {
        val r = slaveInAsyncCall(param)
        return r.await()
    }

    @GetMapping("/asyncSlaveAb")
    suspend fun asyncSlaveAb(): Long {
        val param = getAsyncFsContext(coroutineContext).uuid
        val r = slaveInAsyncCall(param)
        return r.await()
    }

    @GetMapping("/complexAb")
    suspend fun complexAb(): Long {
        val param = getAsyncFsContext(coroutineContext).uuid
        val r = slaveInAsyncCall(param)
        return r.await()
    }

    @GetMapping("/slave")
    suspend fun slaveAbnormal(@RequestParam(value = "value") param: String): Long {
        val (r1, r2) = awaitAsyncOutsideOfSlave(param)
        return r1 + r2
    }

    @GetMapping("/slaveAb")
    suspend fun slaveAbnormalAb(): Long {
        val param = getAsyncFsContext(coroutineContext).uuid
        val (r1, r2) = awaitAsyncOutsideOfSlave(param)
        return r1 + r2
    }

    @GetMapping("/context")
    suspend fun context(): Long {
        putContext("key", getAsyncFsContext(coroutineContext).uuid)
        val r = asAsync {
            personService.readWithContext("key")
        }
        return r.await()
    }

    @GetMapping("/context2")
    suspend fun context2(): Long {
        coroutineContext.getAsyncContext().putData("a", coroutineContext.getAsyncContext().uuid)
        val a = coroutineContext.asAsync {
            val b = coroutineContext.withAsyncContext(coroutineContext.getAsyncContext().uuid) {
                val b = coroutineContext.asAsync {
                    if (coroutineContext.getAsyncContext().uuid != it) {
                        throw Exception("not equal 1")
                    }
                    0L
                }
                val c = coroutineContext.asAsync {
                    coroutineContext.withAsyncContext(coroutineContext.getAsyncContext().uuid) {
                        if (coroutineContext.getAsyncContext().uuid != it) {
                            throw Exception("not equal 2")
                        }
                    }
                    0L
                }
                b.await() + c.await()
            }
            b
        }
        return a.await()
    }

    private suspend fun awaitAsyncOutsideOfSlave(param: String): Pair<Long, Long> {
        val slave = withSlave(param) { value ->
            if (value != param) {
                throw Exception("@$value - $param")
            }

            debugPrint("Slave1", param)
            val a = asAsync {
                // 요거는 안됨
                withSlave(param) { value ->
                    debugPrint("Slave1", param)
                    if (coroutineContext.getUuid() != value) {
                        throw Exception("not eq")
                    }
                    personService.read("slave1")
                }
            }

            val b = asAsync {
                debugPrint("Slave2", param)
                if (coroutineContext.getUuid() != value) {
                    throw Exception("not eq")
                }
                personService.read("slave2")
            }

            Pair(a, b)
        }
        debugPrint("Master", param)
        FsDispatcher.asContext(param) {
            println("master - " + coroutineContext.getAsyncContext().isSlave())
            personService.write()
        }
        personService.read("master")

        val r1 = slave.first.await()
        val r2 = slave.second.await()
        return Pair(r1, r2)
    }

    private suspend fun slaveInAsyncCall(param: String): Deferred<Long> {
        val r = asAsync {
            withSlave(param) { value ->
                if (coroutineContext.getUuid() != value) {
                    throw Exception("not eq")
                }
                personService.read("slave1")
            }

            withSlave(param) { value ->
                if (coroutineContext.getUuid() != value) {
                    throw Exception("not eq")
                }
                personService.read("slave2")
            }
        }

        debugPrint("Master", param)
        personService.read("master")
        return r
    }

    private suspend fun asyncCall(param: String): Long {
        val a = asAsync {
            debugPrint("Async", param)
            personService.read("master")
        }
        val b = asAsync {
            debugPrint("Async", param)
            personService.read("master")
        }
        val r = a.await() + b.await()
        return r
    }

    private suspend fun syncCall(param: String): Long {
        coroutineContext.withAsyncContextAsThreaded(param) {
            debugPrint("Sync", param)
            personService.read("master")
            debugPrint("Sync", param)
            personService.read("master")
        }
        return 0L
    }
}

@Service
class PersonService(val personRepository: PersonRepository) {

    suspend fun read(uuid: String): Long {
        Thread.sleep(100)
        return personRepository.countByName(uuid)
    }

    @Transactional
    suspend fun write() = personRepository.save(Person(name = UUID.randomUUID().toString()))

    suspend fun readWithContext(key: String): Long {
        val value = getContext(key)
        return personRepository.countByName(value as String)
    }
}
