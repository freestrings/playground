package fs.playground

import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class Services(
        private val personRepository: PersonRepository,
        private val animalRepository: AnimalRepository
) {

    @Transactional
    fun personInsert(seq: Long) {
        personRepository.save(Person(personSeq = seq, personType = "A", personName = "person$seq"))
    }

    @Transactional
    fun animalInsert(seq: Long) {
        animalRepository.save(Animal(animalSeq = seq, animalType = "A", animalName = "animal$seq"))
    }
}