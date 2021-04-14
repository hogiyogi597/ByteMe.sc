package com.github.hogiyogi597.persistence

import com.github.hogiyogi597.yarn.YarnResult
import dissonance.data.Snowflake

case class UserSearchState(results: List[YarnResult], webhookId: Snowflake, messageId: Snowflake)
