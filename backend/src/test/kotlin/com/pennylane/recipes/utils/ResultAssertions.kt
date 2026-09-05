package com.pennylane.recipes.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.expect
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import org.assertj.core.api.Assertions.assertThat

fun <V, E> Result<V, E>.assertOk(block: (V) -> Unit = {}) {
    val value = expect { AssertionError("Expected Ok but was Err: $this") }
    block(value)
}

fun <V, E> Result<V, E>.assertErr(expected: E? = null) {
    val error = getError()
    assertThat(error).withFailMessage("Expected Err but was Ok: ${this.get()}").isNotNull()
    if (expected != null) assertThat(error).isEqualTo(expected)
}
