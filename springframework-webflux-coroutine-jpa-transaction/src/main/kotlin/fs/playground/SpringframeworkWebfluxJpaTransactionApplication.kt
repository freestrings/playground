package fs.playground

import fs.playground.FsDispatcher.asAsync
import fs.playground.FsDispatcher.asContext
import fs.playground.FsDispatcher.getAsyncFsContext
import fs.playground.FsDispatcher.getContext
import fs.playground.FsDispatcher.putContext
import fs.playground.FsDispatcher.withSlave
import fs.playground.repository.PersonRepository
import kotlinx.coroutines.Deferred
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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

    @GetMapping("/slave")
    suspend fun slaveAbnormal(@RequestParam(value = "value") param: String): Long {
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

    private suspend fun awaitAsyncOutsideOfSlave(param: String): Pair<Long, Long> {
        val slave = withSlave(param) { value ->
            if (value != param) {
                throw Exception("@$value - $param")
            }

            debugPrint("Slave1", param)
            val a = asAsync {
                withSlave(param) {
                    debugPrint("Slave1", param)
                    personService.read("slave1")
                }
            }

            val b = asAsync {
                debugPrint("Slave2", param)
                personService.read("slave2")
            }

            Pair(a, b)
        }

        debugPrint("Master", param)
        personService.read("master")

        val r1 = slave.first.await()
        val r2 = slave.second.await()
        return Pair(r1, r2)
    }

    private suspend fun slaveInAsyncCall(param: String): Deferred<Long> {
        val r = asAsync {
            withSlave(param) { value ->
                personService.read("slave1")
            }

            withSlave(param) { value ->
                personService.read("slave2")
            }
        }

        debugPrint("Master", param)
        personService.read("master")
        return r
    }

    private suspend fun asyncCall(param: String) = asContext(param) { value ->
        if (value != param) {
            throw Exception("@$value - $param")
        }

        debugPrint("Call1", param)
        val a = asAsync {
            debugPrint("Async", param)
            personService.read("master")
        }
        val r = a.await()
        debugPrint("Call2", param)
        r
    }
}

@Service
class PersonService(val personRepository: PersonRepository) {

    suspend fun read(uuid: String) = personRepository.countByName(uuid)

    suspend fun readWithContext(key: String): Long {
        val value = getContext(key)
        return personRepository.countByName(value as String)
    }
}
