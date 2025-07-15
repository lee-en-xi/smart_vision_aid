

import org.json.JSONArray
import org.json.JSONObject

data class OcrResultItem(
    val text: String,
    val coordinates: List<Float>
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("text", text)
            put("coordinates", JSONArray(coordinates))
        }
    }
}