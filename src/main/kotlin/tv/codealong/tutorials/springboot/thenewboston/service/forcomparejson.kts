
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

/**
* метод для сравнения JSON-ов по содержимому, в не зависимости расположения элементов
* содержимого.
*/

fun compareJsonByValue(node1: JsonNode, node2: JsonNode): Boolean{
	val normalized1 = normalizeJson(node1)
	println(normalized1)
	val normalized2 = normalizeJson(node2)
	println(normalized2)
	return normalized1 == normalized2
}

private fun normalizeJson(node: JsonNode): Any {
	if(node.isObject){
		val normalized = mutableMapOf<String, Any>()
		node.fields().forEach { (key, value) -> 
			normalized[key] = normalizeJson(value)
		}
		return normalized.toSortedMap(compareBy { it })
	} else if(node.isArray){
		val normalized = mutableListOf<Any>()
		node.elements().forEach {
			normalized.add(normalizeJson(it))
		}
		return normalized.sortedWith(compareBy {it.toString()})
	} else {
		return when {
			node.isTextual -> node.textValue()
			node.isBoolean -> node.booleanValue()
			node.isInt -> node.intValue()
			node.isLong -> node.longValue()
			node.isDouble -> node.doubleValue()
			node.isFloat -> node.floatValue()
			else -> node.toString()
		}
	}
}
