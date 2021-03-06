package kales.sample

import kales.KalesApplication
import kales.actionview.ApplicationLayout
import kales.sample.app.controllers.PostsController

fun <T : ApplicationLayout> routes(): KalesApplication<T>.() -> Unit = {
  get<PostsController>("/", "index")
  get<PostsController>("/post/{id}", "show")
  get<PostsController>("/posts/new", "new")
}
