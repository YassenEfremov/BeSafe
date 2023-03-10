package com.example.besafeapp.data

import com.example.besafeapp.model.SecurityTopic

class Datasource{
    fun loadTopics(): List<SecurityTopic> {
        return return listOf<SecurityTopic>(SecurityTopic(1, "Използване на сигурна парола", "Препоръчително е паролата ви да съдържа минимум 12 символа, които да бъдат малки и големи букви, цифри и други (специални) символи. Все пак при създаването на силна парола не е толкова важно да се използват символи от четирите изброени категории, колкото да бъде възможно по-дълга."),
            SecurityTopic(2, "Аутентикация в две или повече стъпки", "Този тип аутентикация добавя още един слой при достъп до даден акаунт, като изисква втори фактор като код пратен по имейла в допълнение на паролата."),
            SecurityTopic(3, "Редовна актуализация на софтуера", "Редовното актуализиране на различните приложения ги поддържа в крак с най-новите мерки за сигурност"),
            SecurityTopic(4, "Използване на различни пароли за различните платформи", "Използването на различни пароли означава, че при изтичането на някоя от тях, ние губим достъп до една платформа, но всички останали са сигурни."),
            SecurityTopic(5, "Не отварям съмнителни линкове и не свалям съмнителни файлове", "Възможно е линковете привидно да приличат на официалните такива. Например може да се замене една буква от линк на верига магазини с подобно изглеждаща, за да може хората да клекнат върху него."),
            SecurityTopic(6, "Използвам VPN (Виртуална Частна Мрежа)", "")
        )
    }
}