package com.daffaadityapurwanto.securein.fragmentdashboard

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.daffaadityapurwanto.securein.R
import com.daffaadityapurwanto.securein.aboutusmenu
import com.daffaadityapurwanto.securein.data.CurrentUser
import com.daffaadityapurwanto.securein.halamanlogin
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout dan kembalikan view-nya
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }
    private lateinit var tombolaboutus: LinearLayout
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tombolkeluar = view.findViewById<LinearLayout>(R.id.keluarsetting)
        tombolkeluar.setOnClickListener {
            Intent(context, halamanlogin::class.java).also {
                startActivity(it)

            }

        }
        val tombolaboutus = view.findViewById<LinearLayout>(R.id.aboutus)
        tombolaboutus.setOnClickListener {
            Intent(context, aboutusmenu::class.java).also {
                startActivity(it)

            }

        }
        val exportfileunencrypted = view.findViewById<LinearLayout>(R.id.exporttofileunencrypted)
        exportfileunencrypted.setOnClickListener {
            try {
                val dbFile = requireContext().getDatabasePath("secureindb.db")
                val outFile = File(requireContext().getExternalFilesDir(null), "secureindbbackup.db")

                FileInputStream(dbFile).channel.use { src ->
                    FileOutputStream(outFile).channel.use { dst ->
                        dst.transferFrom(src, 0, src.size())
                    }
                }

                Toast.makeText(requireContext(), "Database berhasil diekspor ke:\n${outFile.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Gagal ekspor DB: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
