package com.github.hogiyogi597.yarn

import cats.Id
import cats.syntax.all._
import com.github.hogiyogi597.yarn.TestYarnParser._
import weaver._

object JsoupYarnBrowserTest extends SimpleIOSuite {
  implicit val testYarnParser = TestYarnParser
  private val yarn            = new JsoupYarnBrowser[Id]()

  pureTest("getPopular should return the head of parsed YarnResults") {
    expect(yarn.getPopular == yarnResult1.some)
  }

  pureTest("searchTerm should return the head of parsed YarnResults") {
    expect(yarn.searchTerm("dummy") == yarnResult1.some)
  }

  pureTest("multiSearchTerm should return the list of parsed YarnResults") {
    expect(yarn.multiSearchTerm("dummy") == List(yarnResult1, yarnResult2))
  }
}
