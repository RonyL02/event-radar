package com.col.eventradar.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.maplibre.geojson.Geometry
import org.maplibre.geojson.MultiPolygon
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import java.lang.reflect.Type

object GeoJsonParser {
    val gson: Gson =
        GsonBuilder()
            .registerTypeAdapter(Geometry::class.java, GeometryDeserializer())
            .registerTypeAdapter(Geometry::class.java, GeometrySerializer())
            .create()
}

class GeometrySerializer : JsonSerializer<Geometry> {
    override fun serialize(
        src: Geometry?,
        typeOfSrc: Type?,
        context: JsonSerializationContext,
    ): JsonElement =
        if (src != null) {
            JsonParser.parseString(src.toJson())
        } else {
            JsonNull.INSTANCE
        }
}

class GeometryDeserializer : JsonDeserializer<Geometry> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Geometry {
        val jsonObject = json.asJsonObject
        return when (val type = jsonObject.get("type").asString) {
            "Point" -> context.deserialize<Point>(json, Point::class.java)
            "Polygon" -> context.deserialize<Polygon>(json, Polygon::class.java)
            "MultiPolygon" -> context.deserialize<MultiPolygon>(json, MultiPolygon::class.java)
            else -> throw JsonParseException("Unknown geometry type: $type")
        }
    }
}
