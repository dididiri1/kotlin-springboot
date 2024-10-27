# 섹션8. 

## 추가 - 테스트와 영속성 컨텍스트

### [방법 1] @Transactional
장점 : 간결하다, 롤백도 된다, 트랙잭션별로 테스트를 격리할 수 있어, 병렬 테스트도 가능하다.
단점 : 테스트 내성이 떨어진다.
``` 
    ```

    @Transactional
    @DisplayName("유저 1명과 책 2권을 저장하고 대출한다")
    @Test
    fun saveUserAndLoanTwoBooksTest () {
        // when
        userService.saveUserAndLoanTwoBooks()

        // then
        val users = userRepository.findAll()
        assertThat(users).hasSize(1)
        assertThat(users[0].userLoanHistories).hasSize(2)
    }
```


### [방법 2] N쪽의 Repository를 활용한다.

```
    @Transactional
    @DisplayName("유저 1명과 책 2권을 저장하고 대출한다")
    @Test
    fun saveUserAndLoanTwoBooksTest () {
        // when
        userService.saveUserAndLoanTwoBooks()

        // then
        val users = userRepository.findAll()
        assertThat(users).hasSize(1)
        val histories = userLoanHistoryRepository.findAll()
        assertThat(histories).hasSize(2)
        assertThat(histories[0].user.id).isEqualTo(users[0].id)
        
        //assertThat(users[0].userLoanHistories).hasSize(2)
    }
```

### [방법 3] CleaningSpringBootTest를 이용한다.
```
@SpringBootTest
class CleaningSpringBootTest {

    @Autowired
    private lateinit var repositories: List<JpaRepository<*, *>>

    @AfterEach
    fun clean() {
        val currentMillis = System.currentTimeMillis()
        repositories.forEach { it.deleteAll() }
        println("소요 시간 : ${System.currentTimeMillis() - currentMillis}")
    }
}
```
### [방법 4] TxHelper를 이용한다.
txHelper.exec으로 lambda 익명함수를 깜사주는 코딩을 하게 되면
여기 있는 곳은 트랜잭셔널에 존재하는 환경이 된다.  
즉, 트랜잭셔널을 테스트에서만 별도로 걸어주는 방식
```
@Component
class TxHelper {

    @Transactional
    fun exec(block: () -> Unit) {
        block()
    }
}
```

```
@SpringBootTest
class UserServiceTest @Autowired constructor(

    ``` 
    
    private val txHelper: TxHelper,
) {
    
    ```
    
    @Transactional
    @DisplayName("유저 1명과 책 2권을 저장하고 대출한다")
    @Test
    fun saveUserAndLoanTwoBooksTest () {
        // when
        userService.saveUserAndLoanTwoBooks()

        // then
        txHelper.exec {
            val users = userRepository.findAll()
            assertThat(users).hasSize(1)
            assertThat(users[0].userLoanHistories).hasSize(2)
        }
    }
}
```


### Reference
* [실전! 코틀린과 스프링 부트로 도서관리 애플리케이션 개발하기 (Java 프로젝트 리팩토링)](https://inf.run/5j8n)