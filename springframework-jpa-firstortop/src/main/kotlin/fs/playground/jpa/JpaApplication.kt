package fs.playground.jpa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.repository.CrudRepository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@SpringBootApplication
class JpaApplication

/**
 * https://github.com/spring-projects/spring-data-commons/blob/master/src/main/java/org/springframework/data/repository/query/parser/PartTree.java#L279
 * 정규식에서 first와 top을 동일하게 취급: "(First|Top)(\\d*)?";
 */
fun main(args: Array<String>) {
	val ctx = runApplication<JpaApplication>(*args)
	val userRepository = ctx.getBean(UserRepository::class.java)

	/**
	 * --spring.profiles.active=oracle
	 */
//	/* select
//        generatedAlias0
//    from
//        Usera as generatedAlias0
//    where
//        generatedAlias0.name=:param0 */ select
//	*
//	from
//	( select
//			usera0_.id as id1_0_,
//	usera0_.name as name2_0_
//	from
//	usera usera0_
//			where
//	usera0_.name=? )
//	where
//	rownum <= ?

	/**
	 * --spring.profiles.active=mysql
	 */
//	/* select
//        generatedAlias0
//    from
//        Usera as generatedAlias0
//    where
//        generatedAlias0.name=:param0 */ select
//	usera0_.id as id1_0_,
//	usera0_.name as name2_0_
//	from
//	usera usera0_
//			where
//	usera0_.name=? limit ?

	/**
	 * --spring.profiles.active=
	 */
//	/* select
//        generatedAlias0
//    from
//        Usera as generatedAlias0
//    where
//        generatedAlias0.name=:param0 */ select
//	usera0_.id as id1_0_,
//	usera0_.name as name2_0_
//	from
//	usera usera0_
//			where
//	usera0_.name=? limit ?
	userRepository.findFirstByName("name1")
	userRepository.findTopByName("name2")
}

@Entity
data class Usera(@Id
				@GeneratedValue(strategy = GenerationType.AUTO)
				var id: Int? = null,
				 var name: String)

interface UserRepository : CrudRepository<Usera, Int> {
	fun findTopByName(name: String): Usera?
	fun findFirstByName(name: String): Usera?
}