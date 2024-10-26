# 섹션6. 세 번쨰 요구사항 추가하기 - 책 통계

## 32강. 책 통계 보여주기 - 프로덕션 코드 개발

1. SQL의 다양한 기능들 (sum, avg, count, group by, order by)을 이해한다.
2. 간결한 함수형 프로그래밍 기법을 사용해보고 익숙해진다.
3. 동일한 기능을 애플리케이션과 DB로구현해보고, 차이점을 이해한다.

```
    @Transactional(readOnly = true)
    fun getBookStatistics(): List<BookStatResponse> {
        val results = mutableListOf<BookStatResponse>()
        val books = bookRepository.findAll()
        for (book in books) {
            val targetDto = results.firstOrNull { dto -> book.type == dto.type }
            if (targetDto == null) {
                results.add(BookStatResponse(book.type, 1))
            } else {
                targetDto.plusOne()
            }
        }
        return results
    }
```

```
class BookStatResponse(
    val type: BookType,
    var count: Int,
) {
    fun plusOne() {
        count++
    }
}
```

### 리펙토링
```
@Transactional(readOnly = true)
    fun getBookStatistics(): List<BookStatResponse> {
        val results = mutableListOf<BookStatResponse>()
        val books = bookRepository.findAll()
        for (book in books) {
            results.firstOrNull { dto -> book.type == dto.type }?.plusOne()
                ?: results.add(BookStatResponse(book.type, 1))
        }
        return results
    }
```
> 물음표 점은 앞에 있는 값이 null이 아닌 경우에만 실행되기 떄문에 즉 존재하는 경우에는 플러스 1을 해주는 거고  
> 그 다음에 Elvis 연산자로 존재하지 않은 경우에는 if안에 썼던 count 1을 가진 새로운 DTO를 만들어  
> result에 넣어주는 로직을 넣은 다음에 나머지 부분을 모두 지워줄 수 있다.
> NULL 관련된 처리들을 사용하면 전 코드에 길었던 if-else을 깔끔히 만들수 있다.

## 33강. 책 통계 보여주기 - 테스트 코드 개발과 리팩토링

```
    @Test
    @DisplayName("분야별 책 권수를 정상 확인한다")
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(listOf(
            Book.fixture("A", BookType.COMPUTER),
            Book.fixture("B", BookType.COMPUTER),
            Book.fixture("C", BookType.SCIENCE),
        ))


        // when
        val results = bookService.getBookStatistics()

        // then
        assertThat(results).hasSize(2)
        val computerDto = results.first { result -> result.type == BookType.COMPUTER }
        assertThat(computerDto.count).isEqualTo(2)

        val scienceDto = results.first { result -> result.type == BookType.SCIENCE }
        assertThat(scienceDto.count).isEqualTo(1)
    }
```

### 조금더 깔금하게 테스트 코드 관리
```
@Test
    @DisplayName("분야별 책 권수를 정상 확인한다")
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(listOf(
            Book.fixture("A", BookType.COMPUTER),
            Book.fixture("B", BookType.COMPUTER),
            Book.fixture("C", BookType.SCIENCE),
        ))


        // when
        val results = bookService.getBookStatistics()

        // then
        assertThat(results).hasSize(2)
        assertCount(results, BookType.COMPUTER, 2)
        assertCount(results, BookType.SCIENCE, 1)
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Int) {
        assertThat(results.first { result -> result.type == type }.count).isEqualTo(count)
    }
```

### 기존 코드
```
class BookStatResponse(
    val type: BookType,
    var count: Int,
) {
    fun plusOne() {
        count++
    }
}

@Service
class BookService(
        
    ```
    
    ) {
     
    ```
    
    @Transactional(readOnly = true)
    fun getBookStatistics(): List<BookStatResponse> {
        val results = mutableListOf<BookStatResponse>()
        val books = bookRepository.findAll()
        for (book in books) {
            results.firstOrNull { dto -> book.type == dto.type }?.plusOne()
                ?: results.add(BookStatResponse(book.type, 1))
        }
        return results
    }
    
```

### 프로덕션 코드 리펙토링 - group by
- 함수형 프로그래밍을 사용해서 리팩토링
```
class BookStatResponse(
    val type: BookType,
    val count: Int,
) 

@Service
class BookService(
        
    ```
    
    ) {
     
    ```
    
    @Transactional(readOnly = true)
    fun getBookStatistics(): List<BookStatResponse> {
        return bookRepository.findAll() // List<Book>
            .groupBy { book -> book.type } // Map<BookType, List<Book>>
            .map { (type, book) -> BookStatResponse(type, book.size) } // List<BookStatResponse>
    }
    
```

## 35강. 애플리케이션 대신 DB로 기능 구현하기

### 대출 권수 - 기존과 어떤 차이가 있을까?
```
@Transactional(readOnly = true)
fun countLoanedBook(): Int {
   return userLoanHistoryRepository.findAllByStatus(UserLoanStatus.LOANED).size
}
```

### Count - 리팩토링
- DB로부터 숫자를 가져온다
- 적절히 타입을 변환해준다. Long -> int 로 변경
```
@Transactional(readOnly = true)
fun countLoanedBook(): Int {
   return userLoanHistoryRepository.countByStatus(UserLoanStatus.LOANED).toInt()
}
```

### 기존 코드
```
    @Transactional(readOnly = true)
    fun getBookStatistics(): List<BookStatResponse> {
        return bookRepository.findAll() // List<Book>
            .groupBy { book -> book.type } // Map<BookType, List<Book>>
            .map { (type, book) -> BookStatResponse(type, book.size) } // List<BookStatResponse>
    }
```

### 변경후 코드 - JPQL
- DB로부터 숫자를 가져온다
- 적절히 타입을 변환해준다. Long -> int 로 변경
```
@Query("SELECT NEW com.group.libraryapp.dto.book.request.reponse.BookStatResponse(b.type, COUNT(b.id)) FROM Book b GROUP BY b.type")
fun getStats(): List<BookStatResponse>
```

```
    @Transactional(readOnly = true)
    fun getBookStatistics(): List<BookStatResponse> {
        return bookRepository.getStats()
    }
```

### Reference
* [실전! 코틀린과 스프링 부트로 도서관리 애플리케이션 개발하기 (Java 프로젝트 리팩토링)](https://inf.run/5j8n)