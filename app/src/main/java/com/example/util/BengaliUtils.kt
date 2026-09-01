package com.example.util

import java.text.DecimalFormat

object BengaliUtils {

    private val englishToBengaliDigitsMap = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )

    private val bengaliToEnglishDigitsMap = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(englishToBengaliDigitsMap[ch] ?: ch)
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(bengaliToEnglishDigitsMap[ch] ?: ch)
        }
        return sb.toString()
    }

    fun formatBengaliCurrency(amount: Double): String {
        val df = DecimalFormat("#,##0.##")
        val formattedNumber = df.format(amount)
        return toBengaliDigits(formattedNumber)
    }

    fun parseBengaliNumber(input: String): Double {
        val engString = toEnglishDigits(input).replace(",", "").trim()
        val direct = engString.toDoubleOrNull()
        if (direct != null) return direct

        val regex = Regex("""\d+(\.\d+)?""")
        val match = regex.find(engString)
        return match?.value?.toDoubleOrNull() ?: 0.0
    }

    fun parseBengaliInt(input: String): Int {
        return parseBengaliNumber(input).toInt()
    }

    fun formatPresetDisplayText(preset: QuickPreset): String {
        val name = preset.name.trim()
        val qty = preset.defaultQty.trim()
        val priceRaw = preset.defaultRate.ifBlank { preset.defaultAmount }.trim()

        val qtyFormatted = if (qty.isNotBlank()) toBengaliDigits(qty) else ""
        val priceFormatted = if (priceRaw.isNotBlank()) {
            "টাকা: ${toBengaliDigits(priceRaw)}৳"
        } else ""

        val infoPart = when {
            qtyFormatted.isNotBlank() && priceFormatted.isNotBlank() -> "$qtyFormatted | $priceFormatted"
            qtyFormatted.isNotBlank() -> qtyFormatted
            priceFormatted.isNotBlank() -> priceFormatted
            else -> ""
        }

        return if (infoPart.isNotBlank()) {
            "$name ($infoPart)"
        } else {
            name
        }
    }

    val defaultQuickPresets = emptyList<QuickPreset>()
}

data class QuickPreset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val defaultQty: String = "",
    val defaultRate: String = "",
    val defaultAmount: String = "",
    val isFolder: Boolean = false,
    val children: List<QuickPreset> = emptyList()
)

data class FolderOption(
    val id: String,
    val displayName: String,
    val level: Int = 0
)

fun List<QuickPreset>.getAllFlatItems(): List<QuickPreset> {
    val result = mutableListOf<QuickPreset>()
    for (preset in this) {
        if (preset.isFolder) {
            result.addAll(preset.children.getAllFlatItems())
        } else {
            result.add(preset)
        }
    }
    return result
}

fun List<QuickPreset>.getFolderOptions(
    excludeFolderId: String? = null,
    parentPath: String = "",
    level: Int = 0
): List<FolderOption> {
    val list = mutableListOf<FolderOption>()
    if (level == 0) {
        list.add(FolderOption(id = "", displayName = "🏠 প্রধান তালিকা (Main List)", level = 0))
    }
    for (node in this) {
        if (node.isFolder && node.id != excludeFolderId) {
            val currentPath = if (parentPath.isEmpty()) node.name else "$parentPath > ${node.name}"
            list.add(FolderOption(id = node.id, displayName = currentPath, level = level + 1))
            list.addAll(node.children.getFolderOptions(excludeFolderId, currentPath, level + 1))
        }
    }
    return list
}

fun List<QuickPreset>.findParentFolderId(nodeId: String): String? {
    for (node in this) {
        if (node.id == nodeId) return ""
        if (node.isFolder) {
            if (node.children.any { it.id == nodeId }) return node.id
            val subParent = node.children.findParentFolderId(nodeId)
            if (subParent != null) return subParent
        }
    }
    return null
}

fun List<QuickPreset>.removeNodeById(nodeId: String): List<QuickPreset> {
    val result = mutableListOf<QuickPreset>()
    for (node in this) {
        if (node.id == nodeId || (!node.isFolder && node.name.equals(nodeId, ignoreCase = true))) {
            continue
        }
        if (node.isFolder) {
            val updatedChildren = node.children.removeNodeById(nodeId)
            result.add(node.copy(children = updatedChildren))
        } else {
            result.add(node)
        }
    }
    return result
}

fun List<QuickPreset>.addNodeToParent(node: QuickPreset, targetFolderId: String?): List<QuickPreset> {
    if (targetFolderId.isNullOrBlank() || targetFolderId == "ROOT") {
        return listOf(node) + this
    }
    return this.map { current ->
        if (current.isFolder && current.id == targetFolderId) {
            current.copy(children = listOf(node) + current.children)
        } else if (current.isFolder) {
            current.copy(children = current.children.addNodeToParent(node, targetFolderId))
        } else {
            current
        }
    }
}

fun List<QuickPreset>.updateOrMoveNode(
    nodeId: String,
    updatedNode: QuickPreset,
    targetFolderId: String?
): List<QuickPreset> {
    val treeWithoutNode = this.removeNodeById(nodeId)
    return treeWithoutNode.addNodeToParent(updatedNode, targetFolderId)
}

fun List<QuickPreset>.reorderNodesInParent(
    parentFolderId: String?,
    fromIndex: Int,
    toIndex: Int
): List<QuickPreset> {
    if (parentFolderId.isNullOrBlank() || parentFolderId == "ROOT") {
        if (fromIndex !in indices || toIndex !in indices || fromIndex == toIndex) return this
        val mutable = this.toMutableList()
        val moved = mutable.removeAt(fromIndex)
        mutable.add(toIndex, moved)
        return mutable
    }
    return this.map { node ->
        if (node.isFolder && node.id == parentFolderId) {
            val children = node.children.toMutableList()
            if (fromIndex in children.indices && toIndex in children.indices && fromIndex != toIndex) {
                val moved = children.removeAt(fromIndex)
                children.add(toIndex, moved)
            }
            node.copy(children = children)
        } else if (node.isFolder) {
            node.copy(children = node.children.reorderNodesInParent(parentFolderId, fromIndex, toIndex))
        } else {
            node
        }
    }
}

fun parseQuickPresetFromJsonObject(obj: org.json.JSONObject): QuickPreset {
    val id = obj.optString("id", "").ifBlank { java.util.UUID.randomUUID().toString() }
    val name = obj.optString("name", "")
    val defaultQty = obj.optString("defaultQty", "")
    val defaultRate = obj.optString("defaultRate", "")
    val defaultAmount = obj.optString("defaultAmount", "")
    val isFolder = obj.optBoolean("isFolder", false)
    val childrenList = mutableListOf<QuickPreset>()
    val childrenArray = obj.optJSONArray("children")
    if (childrenArray != null) {
        for (i in 0 until childrenArray.length()) {
            val childObj = childrenArray.optJSONObject(i)
            if (childObj != null) {
                val child = parseQuickPresetFromJsonObject(childObj)
                if (child.name.isNotBlank()) {
                    childrenList.add(child)
                }
            }
        }
    }
    return QuickPreset(
        id = id,
        name = name,
        defaultQty = defaultQty,
        defaultRate = defaultRate,
        defaultAmount = defaultAmount,
        isFolder = isFolder,
        children = childrenList
    )
}

fun quickPresetToJsonObject(preset: QuickPreset): org.json.JSONObject {
    val obj = org.json.JSONObject()
    obj.put("id", preset.id)
    obj.put("name", preset.name)
    obj.put("defaultQty", preset.defaultQty)
    obj.put("defaultRate", preset.defaultRate)
    obj.put("defaultAmount", preset.defaultAmount)
    obj.put("isFolder", preset.isFolder)
    if (preset.isFolder) {
        val array = org.json.JSONArray()
        preset.children.forEach { child ->
            array.put(quickPresetToJsonObject(child))
        }
        obj.put("children", array)
    }
    return obj
}
