# 섹션3. Java 서버를 Kotlin 서버로 리팩토링하자!

## 12강. 도메인 계층을 Kotlin으로 변경하기 - Book.java

#### 의존성 주입
```
implementation 'org.jetbrains.kotlin:kotlin-reflect:1.6.21'
```

#### Book.kt
```
import java.lang.IllegalArgumentException
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
class Book(
    val name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다")
        }
    }
}
```

## 13강. 도메인 계층을 Kotlin으로 변경하기 - UserLoanHistory.java, User.java

```
import com.group.libraryapp.domain.user.User
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class UserLoanHistory(

    @ManyToOne
    val user: User,

    val bookName: String,

    var isReturn: Boolean,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    fun doReturn() {
        this.isReturn = true
    }

}
```

```
import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import java.lang.IllegalArgumentException
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class User(

    var name: String,

    val age: Int?,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userLoanHistories: MutableList<UserLoanHistory> = mutableListOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
){

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다")
        }
    }

    fun updateName(name: String) {
        this.name = name
    }

    fun loanBook(book: Book) {
        this.userLoanHistories.add(UserLoanHistory(this, book.name, false))
    }

    fun returnBook(bookName: String) {
        this.userLoanHistories.first { history -> history.bookName == bookName}.doReturn()
    }
}
```

## 14강. Kotlin과 JPA를 함께 사용할 때 이야기거리 3가지


1. setter에 관한 이야기
- setter 대신 좋은 이름의 함수를 사용하는 것이 훨씬 clean하다!
- 하지만 name에 대한 setter는 public 이기 때문에 유저 이름 업데이트 기능에서 setter를 사용할 수도 있다.
- 코드 상 setter가 열려있어서 사용할수도 있다는 것이 불편하다.
- public getter는 필요하기 때문에 setter만 private하게 만드는 것이 최선이다!

```
class User(
  private val _name: String,
) {
  val name: String
     get() = this._name
}       
```
#### 방법 1. backing property 사용하기

```
class User(
  name: String
) {
  val name = name
     private set 
}       
```
#### 방법 2. custom setter 이용하기

> 정리: 하지만 두 방법 모두 프로퍼티가 많아지면 번거롭다!
> 때문에 개인적으로 setter를 열어는 두지만 사용하지 않는 방법을 선호!
> 다행히 현재 팀에서도 setter를 사용하면 안된다는 사실을 모든 개발자 분들이 체득하고 있다.
> Trade-Off의 영역, 팀 컨벤션을 잘 맞추면 되지 않을까!


### 2. 생성자 안의 프로퍼티. 클래스 body 안의 프로퍼티

다시 User 클래스를 보자. user 클래스 주생성자 안에 있는 userLoanHistories와 id는 꼭  
주 생성자 안에 있을 필요가 없다. 아래와 같이 코드가 바뀔 수 있는 것이다


```
@Entity 
class User( 
  var name: String, 
  val age: Int?, 
) { 

  @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true) 
  val userLoanHistories: MutableList<UserLoanHistory> = mutableListOf(),
  
  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY) 
  val id: Long? = null, 
  
  fun updateName(name: String) { 
    this.name = name 
  } 
   
  fun loanBook(book: Book) { 
    this.userLoanHistories.add(UserLoanHistory(this, book.name)) 
  } 
  
  fun returnBook(bookName: String) { 
    this.userLoanHistories.first {history -> history.bookName == bookName }.doReturn() 
   
  } 
}
  
```

그렇다. 위 코드도 잘 동작한다. 그렇다면 어떻케 해야 더 좋을까?
개인적으로는 큰 상관이 없다고 생각한다. 테스트를 하기 위한 객체를 만들어 줄때도 정적  
팩토리 메소드를 사용하다 보니 프로퍼티가 안에 있건, 밖에 있건 두 경우 모두 적절히 대응 할 수 있다.
하지만 명확한 가이드가 있는 것은 함계 개발을 할 때에 중요하므로
1. 모든 프로퍼티를 생성자에 넣거나
2. 프로퍼티를 생성자 혹은 클래스 body 안에 구분해서 넣을 때 명확한 기준이 있거나 해야한다고 생각한다.


### 3. JPA와 data class
- Entity는 data class를 피하는 것이 좋다.
- 그 이유는 equals, hashCode, toString 등의 함수를 자동으로 만들어준다. 사실 원래 세 함수는 JPA Entity와
  그렇케 궁합이 좋치 못하다. 연관관계 상황에서 문제가 될 수 있는 경우들이 존재하기 떄문이다
  예를 들어, 현제 프로젝트에 있는 User와 UserLoanHistory를 생각해보자

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/14_01.png?raw=true)

1:N 연관계를 맺고 있는 상황에서 User쪽에 equals()가 호출된다면 User 는 본인과 관계를 맺고 있는 UserLoanHistory의  
equals()를 호출하게 되고, 다시 UserLoanHistory는 본인과 관계를 맺고 있는 User의 equals()를 호출하게 된다.
때문에 JPA Entitiy는 data class를 피하는 것이 좋다.



마지막으로 작은 TIP 하나를 더 공유하자며 이 TIP 꼭 JPA와 관련된 것은 아니지만, 현재 Kotlin Class가 Domain만 있으니
이 단계를 말하고자 한다.
- Entityu (class) 가 생성되는 로직을 찾고 싶은 경우, constructor 지시어를 명시적으로 작성하고 추적하면 훨씬 편하다.

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/14_02.png?raw=true)

이제 Kotlin으로 변경된 도메인 객체들을 남겨두고, Repository로 넘어가보자!


## 15강. 리포지토리를 Kotlin으로 변경하기


#### Kotlin
```
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface BookRepository : JpaRepository<Book, Long> {

    fun findByName(bookName: String): Optional<Book>
}
  
```

#### Kotlin
```
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    fun findByName(name: String) : Optional<User>
}
```

#### Kotlin
```
import org.springframework.data.jpa.repository.JpaRepository

interface UserLoanHistoryRepository : JpaRepository<UserLoanHistory, Long>{

    fun findByBookNameAndIsReturn(bookName: String, isReturn: Boolean) : UserLoanHistory?
}  
```

## 16강. 서비스 계층을 Kotlin으로 변경하기 - UserService.java

```
plugins {
    id 'org.springframework.boot' version '2.6.8'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.6.21'
    id 'org.jetbrains.kotlin.plugin.jpa' version '1.6.21'
    id 'org.jetbrains.kotlin.plugin.spring' version '1.6.21' / 추가
}
```
#### UserService.java
```
import com.group.libraryapp.domain.user.User;
import com.group.libraryapp.domain.user.UserRepository;
import com.group.libraryapp.dto.user.request.UserCreateRequest;
import com.group.libraryapp.dto.user.request.UserUpdateRequest;
import com.group.libraryapp.dto.user.response.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public void saveUser(UserCreateRequest request) {
    User newUser = new User(request.getName(), request.getAge(), Collections.emptyList(), null);
    userRepository.save(newUser);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getUsers() {
    return userRepository.findAll().stream()
        .map(UserResponse::new)
        .collect(Collectors.toList());
  }

  @Transactional
  public void updateUserName(UserUpdateRequest request) {
    User user = userRepository.findById(request.getId()).orElseThrow(IllegalArgumentException::new);
    user.updateName(request.getName());
  }

  @Transactional
  public void deleteUser(String name) {
    User user = userRepository.findByName(name).orElseThrow(IllegalArgumentException::new);
    userRepository.delete(user);
  }

}
```
#### Kotlin
```
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import com.group.libraryapp.dto.user.response.UserResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    @Transactional
    fun saveUser(request: UserCreateRequest) {
        val newUser = User(request.name, request.age)
        userRepository.save(newUser)
    }

    @Transactional(readOnly = true)
    fun getUsers(): List<UserResponse> {
        return userRepository.findAll()
            .map { user -> UserResponse(user) }
    }

    @Transactional
    fun updateUserName(request: UserUpdateRequest) {
        val user = userRepository.findById(request.id).orElseThrow(::IllegalArgumentException)
        user.updateName(request.name)
    }

    @Transactional
    fun deleteUser(name : String) {
        val user = userRepository.findByName(name).orElseThrow(::IllegalArgumentException)
        userRepository.delete(user)
    }

}
```

## 17강. BookService.java를 Kotlin으로 변경하고 Optional 제거하기

#### Kotlin
```
package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,

) {

    @Transactional
    fun saveBook(request: BookRequest) {
        val book = Book(request.name)
        bookRepository.save(book)
    }

    @Transactional
    fun loanBook(request: BookLoanRequest) {
        val book = bookRepository.findByName(request.bookName).orElseThrow(::IllegalArgumentException)
        if (userLoanHistoryRepository.findByBookNameAndIsReturn(request.bookName, false) != null) {
            throw java.lang.IllegalArgumentException("진작 대출되어 있는 책입니다.")
        }

        val user = userRepository.findByName(request.userName).orElseThrow(::IllegalArgumentException)
        user.loanBook(book)

    }

    @Transactional
    fun returnBook(request: BookReturnRequest) {
        val user= userRepository.findByName(request.userName).orElseThrow(::IllegalArgumentException)
        user.returnBook(request.bookName)

    }
}
```

JDK 8의 등장한 옵셔널은 어떤 값이 null 될 수 있는지 없는지를 잘 나타내주기 위해 생겨났다.
Kotlin에서는 언저 자체 타입 시스템이 물음표를 통해서 어떤 값이 null인지 아닌지 잘 알려주기 떄문에 필요가 없다.

```
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    //fun findByName(name: String) : Optional<User>
    
    fun findByName(name: String) : User?
}
```

```
    ```
    
    @Transactional
    fun deleteUser(name : String) {
        val user = userRepository.findByName(name).orElseThrow(::IllegalArgumentException)
        userRepository.delete(user)
    }
    
    
    @Transactional
    fun deleteUser(name : String) {
        val user = userRepository.findByName(name) ?: throw java.lang.IllegalArgumentException()
        userRepository.delete(user)
    }
    
    ```
    
```

### ?: throw IllegalArgumentException() 반복 코드 발생 - 리팩토링

#### ExceptionUtils.kt
```
import java.lang.IllegalArgumentException

fun fail() : Nothing {
    throw IllegalArgumentException()
}
```
```
    ```
    
    @Transactional
    fun returnBook(request: BookReturnRequest) {
        val user= userRepository.findByName(request.userName) ?: fail()
        user.returnBook(request.bookName)

    }
    
    ```
```

#### 확잘 함수를 통해 Optional을 제어할 수 있음

![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/17_01.png?raw=true)

#### CrudRepositoryExtensions.kt
```
fun <T, ID> CrudRepository<T, ID>.findByIdOrNull(id: ID): T? = findById(id).orElse(null)
```

```
    @Transactional
    fun updateUserName(request: UserUpdateRequest) {
        val user = userRepository.findByIdOrNull(request.id) ?: fail()
        user.updateName(request.name)
    }
```

### 확장함수 직접 만들
```
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import java.lang.IllegalArgumentException

fun fail() : Nothing {
    throw IllegalArgumentException()
}

fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID): T {
  return this.findByIdOrNull(id) ?: fail()
}
```

```
    @Transactional
    fun updateUserName(request: UserUpdateRequest) {
        val user = userRepository.findByIdOrThrow(request.id)
        user.updateName(request.name)
    }
```

## 18강. DTO를 Kotlin으로 변경하기
![](https://github.com/dididiri1/kotlin-springboot/blob/main/study/images/18_01.png?raw=true)

#### Kotlin
```
import com.group.libraryapp.domain.user.User

class UserResponse(
    user: User
) {
    val id: Long
    val name: String
    val age: Int?

    init {
        id = user.id!!
        name = user.name
        age = user.age
    }
}

```

#### Kotlin - init에서 부생성자로 리팩토링
```
import com.group.libraryapp.domain.user.User

class UserResponse(
    val id: Long,
    val name: String,
    val age: Int?,
) {

    constructor(user: User) : this(
        id = user.id!!,
        name = user . name,
        age = user.age
    )
}
```

#### Kotlin - 정적팩토리 메소드 리팩토링
```
import com.group.libraryapp.domain.user.User

data class UserResponse( // dto 전체적으로 data 추가
    val id: Long,
    val name: String,
    val age: Int?,
) {

    companion object {
        fun of(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                name = user . name,
                age = user.age)
        }
    }
}
```
> 데이터 트랜스퍼 오브젝트는 즉 DTO는 상황에 따라서 equals나 hashcode 혹은
> toString 등을 디버깅 과정이나 테스트 과정에서 사용할 수도 있다.
> 그래서 DTO를 항상 그 취지에 맞게 data 클래스로 만들어주는 편이다.

## 19. Controller 계층을 Kotlin으로 변경하기

#### Java
```
import com.group.libraryapp.dto.book.request.BookLoanRequest;
import com.group.libraryapp.dto.book.request.BookRequest;
import com.group.libraryapp.dto.book.request.BookReturnRequest;
import com.group.libraryapp.service.book.BookService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @PostMapping("/book")
  public void saveBook(@RequestBody BookRequest request) {
    bookService.saveBook(request);
  }

  @PostMapping("/book/loan")
  public void loanBook(@RequestBody BookLoanRequest request) {
    bookService.loanBook(request);
  }

  @PutMapping("/book/return")
  public void returnBook(@RequestBody BookReturnRequest request) {
    bookService.returnBook(request);
  }

}
```

#### Kotlin
```
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.service.book.BookService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BookController(
    private val bookService: BookService,
) {

    @PostMapping("/book")
    fun saveBook(@RequestBody request: BookRequest) {
        bookService.saveBook(request)
    }

    @PostMapping("/book/loan")
    fun loanBook(@RequestBody request: BookLoanRequest) {
        bookService.loanBook(request)
    }

    @PutMapping("/book/return")
    fun returnBook(@RequestBody request: BookReturnRequest) {
        bookService.returnBook(request)
    }

}
```

코틀린은 함수문법에서 Block Body 쓰지 않고 = 도 가능
```
    @GetMapping("/user")
    fun getUser() : List<UserResponse> = userService.getUsers()
    
```

#### Java
```
import com.group.libraryapp.dto.user.request.UserCreateRequest;
import com.group.libraryapp.dto.user.request.UserUpdateRequest;
import com.group.libraryapp.dto.user.response.UserResponse;
import com.group.libraryapp.service.user.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/user")
  public void saveUser(@RequestBody UserCreateRequest request) {
    userService.saveUser(request);
  }

  @GetMapping("/user")
  public List<UserResponse> getUsers() {
    return userService.getUsers();
  }

  @PutMapping("/user")
  public void updateUserName(@RequestBody UserUpdateRequest request) {
    userService.updateUserName(request);
  }

  @DeleteMapping("/user")
  public void deleteUser(@RequestParam String name) {
    userService.deleteUser(name);
  }

}

```

#### Kotlin
```
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import com.group.libraryapp.dto.user.response.UserResponse
import com.group.libraryapp.service.user.UserService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(

    private val userService: UserService,
) {

    @PostMapping("/user")
    fun saveUser(@RequestBody request: UserCreateRequest) {
        userService.saveUser(request)
    }

    @GetMapping("/user")
    fun getUser() : List<UserResponse> {
        return userService.getUsers()
    }

    @PutMapping("/user")
    fun updateUserName(@RequestBody request: UserUpdateRequest) {
        userService.updateUserName(request)
    }

    @DeleteMapping("/user")
    fun deleteUser(@RequestParam name: String) {
        userService.deleteUser(name)
    }

}
```

#### Kotlin
```
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LibraryAppApplication

fun main(args: Array<String>) {
    runApplication<LibraryAppApplication>(*args)
}
```

Spring Boot는 @RestController 어노테이션이 달린 경우 Jackson 라이브러리가 직렬화와 역직렬화를 담당하게 된다.
변환 과정에서는 큰 문제는 없지만 스프링부트와 Kotlin을 함께 사용하게 된다면 달라진다.
의존성 추가로 해결!

#### build.gradle
```
implementation 'com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2'
```

### Reference
* [실전! 코틀린과 스프링 부트로 도서관리 애플리케이션 개발하기 (Java 프로젝트 리팩토링)](https://inf.run/5j8n)