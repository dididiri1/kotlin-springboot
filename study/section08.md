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

## 추가 - 코프링과 플러그인 

```
id 'org.jetbrains.kotlin.plugin.spring' version '1.6.21'
```
### Spring 플러그인이란
The plugin spectifies the follweing annotations;
- @Componect
- @Async
- @Transactional
- @Cacheable
- SpringBootTest

> Kotlin 공식 문서에 나와 있는 것에 따르면 이렇게 컴포너트 라는 어노테이션이 붙어 있거나 아니면  
> 트랜잭셔널 같은 어노테이션이 붙어있는 클래스나 함수를 자동으로 열어준다.
> Kotlin 에서 open이란 키워드를 붙인다는 것은 클래스를 상속 가능하게 하거나 함수를 오버라이드 가능하게 하는 것이다.

### 왜 open 되어야 하는가!?

#### 예시
```
@Service
class AService {
   
   @Transactional
   fun saveEntity() {
     // 무엇가 로직이 있다!! 
   
   }
 
}
```
> 이게 왜 도대체 오픈되어야 하냐면 서비스는 왜 오픈되어야 하는가  
> 결론부터 말하면 이 피록시라는 것 떄문에 그렇다. 프록시란 여기 있는 a 서비스의 ssaveEntity가
> 불리기 전에 앞뒤로 어떤 로직을 감싸주는 것이다. 예를 들어서 여기 지금 트랜잭션널이라고 되어있으면
> 즉, 이 saveEntity가 시작되기 전에 트랜잭션널을 시작하고 그 다음 로직을 수행시키고 성공하면 커밋, 실패하면  
> rollback이라는 코드를 원래는 개발자가 트랜잭셜널이 없는 상테에서 tyy catch로 로직을 넣는다. 

## [2] JPA 객체와 기본 생성자
```
id 'org.jetbrains.kotlin.plugin.jpa' version '1.6.21'
```
> 이 플러그인의 역할은 Entity 객체나 mapped 슈퍼 클래스 객체 혹은 임베디드 객체으 ㅣ기본 생성자를 만둘어주는 것이다. 
> 그래서 기본 생성자는 왜 만들어야 되냐? 
> JPA 명세서에는 Entity 클래스는 반드시 기본 생성자를 가지고 있어야 한다. 
> 그 이유는 JPA가 동적으로 엔티티 객체 인스턴스를 만들 때 reflection이라는 기술을 활용하는데 
> 이 reflection 이라는 기술을 활용해서 어떤 객체를 인스턴스화 할때 기본 생성자가 필요한 부분들이 있어서 jpa에서는 엔티티  
> 클래스의 기본 생성자를 요구하고 잇다. 근데 코틀린에서는 기본 생성자가 없다. 그래서 기본 생성자를 만들기 어렵다.  
> 그래서 이렇게 플러그인을 활용해서 JPA 객체 기본 생성자를 자동으로 만들어 주도록 했다.

## [3] JPA 객체와 open 

### Reference
* [실전! 코틀린과 스프링 부트로 도서관리 애플리케이션 개발하기 (Java 프로젝트 리팩토링)](https://inf.run/5j8n)