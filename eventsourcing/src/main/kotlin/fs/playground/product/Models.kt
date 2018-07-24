package fs.playground.product

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "product")
data class Product(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
        val name: String,
        @Column(name = "stock_qty") var stockQty: Int
)

interface ProductRepositoryExt {
    fun adjustStockQty(id: Long, amount: Int)
}

interface ProductRepository : JpaRepository<Product, Long>, ProductRepositoryExt

class ProductRepositoryImpl(
        @Autowired val entityManager: EntityManager
) : ProductRepositoryExt {

    override fun adjustStockQty(id: Long, amount: Int) {
        val query = entityManager.createQuery("select p from Product p where id = :id", Product::class.java)
        query.setParameter("id", id)
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE)
        val product: Product? = query.singleResult
        product?.let {
            if (product.stockQty > 0) {
                product.stockQty += amount
                entityManager.persist(product)
            } else {
                throw Exception("넘었음")
            }
        }
    }

}