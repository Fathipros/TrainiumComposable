package com.example.trainium2

import android.util.Log
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

object DatabaseAdmin {
    private const val TAG = "DatabaseAdmin"
    private const val url = "jdbc:mysql://db4free.net:3306/proyectogimnasio?useSSL=false&serverTimezone=UTC&connectTimeout=30000&socketTimeout=30000"
    private const val user = "proyectogimnasio"
    private const val pass = "proyectogimnasio"

    fun connection(): Connection? {
        return try {
            Class.forName("com.mysql.jdbc.Driver")
            val props = Properties()
            props.setProperty("user", user)
            props.setProperty("password", pass)
            props.setProperty("connectTimeout", "30000")
            props.setProperty("socketTimeout", "30000")
            val conn = DriverManager.getConnection(url, props)
            Log.d(TAG, "Conexión establecida correctamente")
            conn
        } catch (e: Exception) {
            Log.e(TAG, "Error de conexión: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}