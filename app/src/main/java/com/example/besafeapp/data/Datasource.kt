package com.example.besafeapp.data

import android.content.Context
import android.util.Log
import com.example.besafeapp.data.Datasource.Companion.FILE_NAME
import com.example.besafeapp.model.SecurityTopic
import org.json.JSONObject
import java.io.*

class Datasource{
    fun loadTopics(): List<SecurityTopic> {
        return return listOf<SecurityTopic>(SecurityTopic(1,
            "Използвам сигурна и дълга парола без да я повтарям на различни места",
            "КАК: Използвам парола, която е минимум 12 символа и съдържа малки и големи букви, цифри и други специални символи.\n\n" +
                "ЗАЩО: Най-често хакерите опитват всички възможни комбинации, докато не стигнат правилната парола. Този метод започва с по-къси предположения и постепенно увеличава дължината им. Затова по-дългите пароли са по-сигурни."),
            SecurityTopic(2,
                "Идентифицирам се в две или повече стъпки",
                "КАК: Освен парола, използвам еднократни кодове, пратени по имейл или SMS, или биометрични данни като пръстов отпечатък или лицево разпознаване.\n\n" +
                        "ЗАЩО: Това добавя още един слой при идентификацията на потребителите, което прави влизането в чужд профил по-трудно."),
            SecurityTopic(3,
                "Редовно актуализирам софтуерните продукти, които използвам",
                "КАК: Редовно проверявам за нови версии и при наличност ги подновявам.\n" +
                        "ЗАЩО: В по-новата версия на даден продукт често се оправят проблеми със сигурността, които са налични в текущата му версия."),
            SecurityTopic(4,
                "Използвам различни пароли за различните платформи",
                "КАК: Използвам виртуален софтуерен мениджър за пароли (тефтер с катинар), който да се грижи за различните пароли, докато аз трябва да помня само ключа за него.\n\n" +
                        "ЗАЩО: Ако една парола изтече, другите ми акаунти ще останат защитени."),
            SecurityTopic(5,
                "Не отварям съмнителни линкове и не свалям съмнителни файлове",
                "КАК: Преглеждам линковете за сменени букви, които изглеждат по същия начин, и дали източникът е достоверен.\n\n" +
                        "ЗАЩО: При отваряне на опасен линк, устройството ни може да се зарази с вирус и личните ни данни могат да бъдат откраднати."),
            SecurityTopic(6,
                "Използвам VPN (Виртуална Частна Мрежа)",
                "КАК: Използвам лицензирано приложение за виртуална частна мрежа, като следвам стъпките, описани в него.\n\n" +
                        "ЗАЩО: По този начин информацията, която изпращаме се криптира и не може да бъде открадната. Виртуалната частна мрежа е силно препоръчителна при свързване към публични мрежи, особено ако те нямат парола.\n"),
            SecurityTopic(7,
            "Не споделям публично лични данни и снимки",
            "КАК: Избягвам публичното споделяне на лични данни, освен ако не е наложително.\n\n" +
                    "ЗАЩО: Те могат да бъдат използвани без нашето знание и съгласие."),
            SecurityTopic(8,
                "Наясно съм, че е възможно някой да се представя за друг онлайн.",
                "КАК: Не вярвам на никого, дори когато изглежда, че го познавам.\n\n" +
                        "ЗАЩО: Възможно е някой да си създаде профил със същото име и снимка като наш познат и да го използва, за да се представя за него с цел извличане на чувствителна информация от нас."),
            SecurityTopic(9,
                "Ползвам антивирусна програма на всяко устройство",
                "КАК: Инсталирам лицензирана антивирусна програма на всяко свое устройство.\n\n" +
                        "ЗАЩО: Подобен софтуер следи и сканира за вируси и нежелани процеси, които се случват на нашето устройство."),
            SecurityTopic(10,
                "Чета условията за ползване и политиките за сигурност на сайтовете, в които се регистрирам.",
                "КАК: Чета политиката на приложенията преди да отбележа, че съм съгласен с тях и ги приемам.\n\n" +
                        "ЗАЩО: В нея са описани данните, до които има достъп приложението и които събира, както и как те са използвани от компанията, предоставяща ни продукта.\n"),
            SecurityTopic(11,
                "Умея да разпознавам фишинг имейли",
                "КАК: Това са съмнителни имейли, които карат потребителя да кликне върху даден линк.\n\n" +
                        "ЗАЩО: Често изпращачът се прави на голяма фирма, с което цели жертвата да повярва на съдържанието и да последва линка, което би я подложило на някакъв вид атака.")

        )
    }

    companion object{
        const val FILE_NAME = "safetyCheck.txt"
    }

    fun readFile(context: Context): JSONObject {
        val file = File(context.filesDir, FILE_NAME)

        val inputStream: InputStream = context.openFileInput(FILE_NAME)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line = reader.readLine()
        }
        reader.close()
        return JSONObject(stringBuilder.toString())
    }

    fun writeFile(string:String, context: Context){
        Log.d("TAG", string)
        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
            it.write(string.toByteArray())
        }
    }
}
