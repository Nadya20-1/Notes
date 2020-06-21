package com.example.notes.utils

import java.util.*
import java.util.regex.Pattern

object StringUtils {
    fun cutStringByImgTag(targetStr: String): List<String> {
        val splitTextList: MutableList<String> =
            ArrayList()
        val pattern =
            Pattern.compile("<img.*?src=\\\"(.*?)\\\".*?>")
        val matcher = pattern.matcher(targetStr)
        var lastIndex = 0
        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                splitTextList.add(targetStr.substring(lastIndex, matcher.start()))
            }
            splitTextList.add(targetStr.substring(matcher.start(), matcher.end()))
            lastIndex = matcher.end()
        }
        if (lastIndex != targetStr.length) {
            splitTextList.add(targetStr.substring(lastIndex, targetStr.length))
        }
        return splitTextList
    }

    fun getImgSrc(content: String?): String? {
        var str_src: String? = null
        val p_img =
            Pattern.compile("<(img|IMG)(.*?)(/>|></img>|>)")
        val m_img = p_img.matcher(content)
        var result_img = m_img.find()
        if (result_img) {
            while (result_img) {
                val str_img = m_img.group(2)
                val p_src =
                    Pattern.compile("(src|SRC)=(\"|\')(.*?)(\"|\')")
                val m_src = p_src.matcher(str_img)
                if (m_src.find()) {
                    str_src = m_src.group(3)
                }
                result_img = m_img.find()
            }
        }
        return str_src
    }

    //get text or img info from stored data
    fun getFirstImgPathFromStringData(data: String): String? {
        var firstImagePath: String
        val textList: List<String> = ArrayList()
        val list = cutStringByImgTag(data)
        for (i in list.indices) {
            val text = list[i]
            if (text.contains("<img") && text.contains("src=")) {
                return getImgSrc(text)
            }
        }
        return ""
    }

    fun getTextFromStringData(data: String): String {
        var text = ""
        val list =
            cutStringByImgTag(data)
        for (i in list.indices) {
            val tx = list[i]
            if (!(tx.contains("<img") && tx.contains("src="))) {
                text = text + tx
            }
        }
        return text
    }
}