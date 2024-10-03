# 1 도서관리 애플리케이션 리팩토링 준비하기
1. Java로 작성된 도서관리 애플리케이션 이해하기
2. 테스트 코드가 무엇인지 왜 필요한지 이해하고, Junit5를 사용해 Spring Boot의 테스트 코드를 작성한다.
3. 실제 만들어진 Java 프로젝트에 대해 Kotlin으로 테스트를 작성하며 Kotlin 코드 작성에 익숙해진다.

### H2 접속
```
spring:
  datasource:
    url: 'jdbc:h2:mem:library'
    username: 'user'
    password: ''
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        format_sql: true
        show_sql: true
h2: console:
      enabled: true
      path: '/h2-console'
```


![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/01_01.png?raw=true)


# 3강. 코틀린 코드 작성 준비하기

### build.gradle 
```
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.6.21'
}
dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
}
compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}
compileTestKotlin {
    kotlinOptions {
        jvmTarget = "11"
} }
```
```
plugins {
    id 'org.springframework.boot' version '2.6.8'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.6.21'
}

group = 'com.group'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8'

    runtimeOnly 'com.h2database:h2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}

compileTestKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}
```

# 4강. 사칙연산 계산기에 대한 테스트 코드 작성하기

## 계산기 요구사항
1. 계산기는 정수만을 취급한다.
2. 계산기가 생성될 때 숫자를 1개 받는다.
3. 최초 숫자가 기록된 이후에는 연산자 함수를 통해 숫자를 받아 지속적으로 계산한다.

### Calculator.kt
```
import java.lang.IllegalArgumentException

data class Calculator(
    private var number: Int,
) {

    fun add(operand: Int) {
        this.number += operand
    }

    fun minus(operand: Int) {
        this.number -= operand
    }

    fun multiply(operand: Int) {
        this.number *= operand
    }

    fun divide(operand: Int) {
        if(operand == 0) {
            throw IllegalArgumentException("0으로 나눌 수 없습니다.")
        }

        this.number /= operand
    }
}
```

### CalculatorTest.kt
```
import java.lang.IllegalArgumentException

fun main() {
    val calculatorTest = CalculatorTest()
    calculatorTest.addTest()
}

class CalculatorTest {

    fun addTest() {
        val calculator = Calculator(5)
        calculator.add(3)

        val expectedCalculator = Calculator(8)
        if (calculator != expectedCalculator) {
            throw IllegalArgumentException()
        }
    }
}
```

#### 이때 계산기 안에 8이 있는지 확인하는 방법 2가지가 있다.
- 첫번째는 data class의 equals를 사용하는 방법이다.
- data class의 경우 자동으로 equals를 만들어 주기 때문에 8을 가지고 잇는 계산기 인스턴스를  
  만든 다음 != 연산자로두 계산기가 같은지 다른지 확인할 수 있다.
```
data class Calculator(
  private var number: Int,
) {
   // 생략...
}
```
```
fun addTest() {
  val calculator = Calculator(5)
  calculator.add(3)
  val expectedResult = Calculator(8)
  if (calculator != expectedResult) {
    throw IllegalStateException()
  }
}
```

- 두 번째 방법은, Calculator로부터 number을 가져오는 방법이다. 현재는 Calculator가
- private number을 가지고 있기 떄문에 calculator.number로 number 프로퍼티를 가져올 수 없다.
- number를 가져오기 위해서는 간단히 private 대신 public을 사용할 수 있다.

```
class Calculator(
  var number: Int,
){
  // 생략...
}
```
Shift+F6 하면 전체 치완    
- 백킹 프로퍼티 방법(Backing Properties)
- 세 번째 방법은 원래 존재하던 number 라는 프로퍼티를 _number 라는 이름으로 변경하고 불변 number를 추가하는 방법이 있다
```
class Calculator(
  private var _number: Int,
){
  val number: Int
    get() = this._number

  fun add(operand: Int) {
    this._number += operand // this._number           .
  }
  
  // 생략... 
}
```

```
import java.lang.IllegalArgumentException

fun main() {
    val calculatorTest = CalculatorTest()
    calculatorTest.addTest()
    calculatorTest.minusTest()
    calculatorTest.multiplyTest()
}

class CalculatorTest {

    fun addTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.add(3)

        // then
        if(calculator.number != 8) {
            throw IllegalArgumentException()
        }
    }
    
    fun minusTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.minus(3)

        // then
        if(calculator.number != 2) {
            throw IllegalArgumentException()
        }
    }
    
    fun multiplyTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.multiply(2)

        // then
        if(calculator.number != 10) {
            throw IllegalArgumentException()
        }
    }
}
```

## 5강. 사칙연산 계산기의 나눗템 테스트 작성
```
fun main() {
    val calculatorTest = CalculatorTest()
    calculatorTest.divideExceptionTest()
}

class CalculatorTest {
    fun divideExceptionTest() {
        // given
        val calculator = Calculator(5)

        // when
        try{
            calculator.divide(0)

        }catch (e: IllegalArgumentException) {
            if(e.message != "0으로 나눌 수 없습니다.") {
                throw IllegalStateException()
            }
            // 테스트 성공!!
            return
        } catch (e: Exception) {
            throw IllegalStateException()
        }
        throw java.lang.IllegalStateException("기대하는 예외가 발생하지 않았습니다.")
    }
}
```
### 수동으로 만든 테스트 코드의 단점
1. 테스트 클래스와 메소드가 생길 때마다 메인 메소드에 수동으로 코드를 작성해주어야 하고,
   메인 메소드거 어주 커진다. 테스트 메소드를 개별적으로 실행하기도 어렵다.
2. 테소트가 실패한 경우 무엇을 기대하였고, 어떤 잘못된 값이 들어와 실패했는지 알려주지 않는다. 
   예외를 던지거나 try catch를 사용해야 하는 등 직접 구현해야 할 부분이 많아 불편하다.
3. 테스트 메소드 별로 공통적으로 처리해야 하는 기능이 있다면, 메소드마다 중복이 생긴다.

## 6강. Junit5 사용법과 테스트 코드 리팩토링
### Junit5에서 사용되는 5가지 어노테이션
- @Test: 테스트 메소드를 지정한다. 테스트 메소드를 실행하는 과정에서 오류가 없으면 성공이다.
- @BeforeEach : 각 테스트 메소드가 수행되기 전에 실행되는 메소드를 지정한다.
- @AfterEach: 각 테스트가 수행된 후에 실행되는 메소드를 지정한다.
- @BeforeAll: 모든 테스트를 수행하기 전에 최초 1회 수행되는 메소드를 지정한다.
- @AfterAll: 모든 테스트를 수행한 후 최후 1회 수행되는 메소드를 지정한다.

```
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JunitCalculatorTest {

    @Test
    fun addTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.add(3)

        // then
        assertThat(calculator.number).isEqualTo(8)
    }

    @Test
    fun minusTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.minus(3)

        // then
        assertThat(calculator.number).isEqualTo(2)
    }

    @Test
    fun multiplyTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.multiply(3)

        // then
        assertThat(calculator.number).isEqualTo(15)
    }

    @Test
    fun divideTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.divide(2)

        // then
        assertThat(calculator.number).isEqualTo(2)
    }

    @Test
    fun divideExceptionTest() {
        // given
        val calculator = Calculator(5)

        // when then
        val message = assertThrows<IllegalArgumentException> {
            calculator.divide(0)
        }.apply {
            assertThat(message).isEqualTo("0으로 나눌 수 없습니다.")    
        }
        

    }
}
```