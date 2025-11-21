package lol.terabrendon.houseshare2.domain.form

import io.github.brendonmendicino.aformvalidator.core.Metadata

sealed interface FormMetadata : Metadata {
    data object Test : FormMetadata
}