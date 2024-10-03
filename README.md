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
