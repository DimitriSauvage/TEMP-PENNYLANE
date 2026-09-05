package com.pennylane.recipes.recipe.adapter.inbound.web.errormanager

import com.pennylane.recipes.recipe.application.error.UseCaseError

/** Bridges a typed application error into the MVC exception channel; mapped to HTTP by [ApplicationErrorHandler]. */
class ApplicationException(
    val error: UseCaseError,
) : RuntimeException(error.message)
