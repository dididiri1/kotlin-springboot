package com.group.libraryapp.dto.user.response

class UserLoanHistoryResponse(
    val name: String,
    val books: List<BookHistoryResponse>,
)

data class BookHistoryResponse(
    val name: String,
    val isReturn: Boolean,
)