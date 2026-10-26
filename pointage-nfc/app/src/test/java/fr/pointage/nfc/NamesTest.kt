package fr.pointage.nfc

import org.junit.Assert.assertEquals
import org.junit.Test

class NamesTest {
    @Test
    fun parsesSttExport() {
        val csv = "﻿activity name,time started,time ended,comment,categories,record tags,duration,duration minutes\r\n" +
            "Montage,a,b,,Travail,\"Dupont, Martin\",1:0:0,60\r\n" +
            "SAV,a,b,\"pièce \"\"X\"\"\r\nà commander\",Travail,Dupont,0:30:0,30\r\n" +
            "SAV,a,b,,,,0:10:0,10\r\n"

        val rows = parseCsv(csv)
        assertEquals(4, rows.size)
        assertEquals("pièce \"X\"\r\nà commander", rows[2][3])

        val (works, clients) = namesFromExport(csv)
        assertEquals(listOf("SAV", "Montage"), works)
        assertEquals(listOf("Dupont", "Martin"), clients)
    }
}
