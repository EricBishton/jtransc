/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.text

class Indenter : ToString {
	interface Action {
		data class Line(val str: String) : Action

		data class LineDeferred(val callback: () -> Indenter) : Action

		object Indent : Action

		object Unindent : Action
	}

	private val actions = arrayListOf<Action>()

	val noIndentEmptyLines = true

	companion object {
		fun genString(init: Indenter.() -> Unit) = gen(init).toString()

		fun gen(init: Indenter.() -> Unit): Indenter {
			val indenter = Indenter()
			indenter.init()
			return indenter
		}

		fun replaceString(templateString: String, replacements: Map<String, String>): String {
			val pattern = Regex("\\$(\\w+)")
			return pattern.replace(templateString) { result ->
				replacements[result.groupValues[1]] ?: ""
			}
		}

		private val INDENTS = arrayListOf<String>("")

		private fun getIndent(index: Int): String {
			if (index >= INDENTS.size) {
				val calculate = INDENTS.size * 10
				var indent = INDENTS[INDENTS.size - 1]
				while (calculate >= INDENTS.size) {
					indent += "\t"
					INDENTS.add(indent)
				}
			}
			return INDENTS[index]
		}
	}

	var out: String = ""

	fun line(indenter: Indenter): Indenter {
		this.actions.addAll(indenter.actions)
		return this
	}

	fun line(str: String): Indenter {
		this.actions.add(Action.Line(str))
		return this
	}

	fun linedeferred(init: Indenter.() -> Unit): Indenter {
		this.actions.add(Action.LineDeferred({
			val indenter = Indenter()
			indenter.init()
			indenter
		}))
		return this
	}

	fun line(str: String, callback: () -> Unit): Indenter {
		line("$str {")
		indent(callback)
		line("}")
		return this
	}

	fun line(str: String, after: String, callback: () -> Unit): Indenter {
		line("$str { $after")
		indent(callback)
		line("}")
		return this
	}

	inline fun indent(callback: () -> Unit): Indenter {
		_indent()
		try {
			callback()
		} finally {
			_unindent()
		}
		return this
	}

	fun _indent() {
		actions.add(Action.Indent)
	}

	fun _unindent() {
		actions.add(Action.Unindent)
	}

	override fun toString(): String {
		val chunks = arrayListOf<String>()

		var indentIndex = 0

		fun eval(actions: List<Action>) {
			for (action in actions) {
				when (action) {
					is Action.Line -> {
						if (noIndentEmptyLines && action.str.isEmpty()) {
							chunks.add("\n")
						} else {
							chunks.add(getIndent(indentIndex))
							chunks.add(action.str)
							chunks.add("\n")
						}
					}
					is Action.LineDeferred -> eval(action.callback().actions)
					Action.Indent -> indentIndex++
					Action.Unindent -> indentIndex--
				}
			}
		}

		eval(actions)

		return chunks.joinToString("")
	}
}
