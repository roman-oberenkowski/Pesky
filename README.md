# Pesky
Projekt komunikatora audio-video na laboratoria z Sieci Komputerowych

## Kompilacja i uruchamianie
- Klient - Java 11 z Mavenem - `mvn javafx:run`.
- Serwer - C11 - `gcc src/main.c src/constants.h src/main_server.c src/main_server.h src/thread.c src/thread.h src/messaging.c src/messaging.h src/structs/list_head.c src/structs/list_head.h src/structs/user.c src/structs/user.h -l pthread -o server -Wall` bądź też z wykorzystaniem make. W celu uruchomienia tak skompilowanego projektu wystarczy uruchomić plik `./server`.

## Protokół komunikacyjny
Wiadomości wysyłane przez projekt mają stałą, ustaloną strukturę `type:<typ_wiadomości>;content:<zawartość>\n`. 

Po nawiązaniu połączenia użytkownik może wysłać następujące typy wiadomości:
- `set_username` - ustawia nazwę użytkownika równą zawartości wiadomości. Nazwa ta musi być unikalna dla całego serwera. 
- `call_to` - użytkownik próbuje zadzwonić do innego użytkownika podłączonego do serwera, o nazwie użytkownika równej zawartości wiadomości. Użytkownik ten musi istnieć na serwerze, każdy z nich musi posiadać unikalną nazwę użytkownika oraz obaj użytkownicy nie mogą być aktualnie połączeni z kimkolwiek. 
- `audio` - użytkownik wysyła jako zawartość wiadomości zserializowany fragment dźwięku, który przekazywany jest do użytkownika, który jest z nim aktualnie połączony.
- `video` - użytkownik wysyła jako zawartość wiadomości zserializowaną klatkę obrazu, która przekazywana jest do użytkownika, który jest z nim aktualnie połączony.
- `disconnect` - użytkownik odłącza się od serwera, rozłączając się z aktualnym rozmówcą.

Serwer odpowiada następującymi typami wiadomości:
- `confirm` - potwierdzenie wykonanej operacji, której opis znajduje się w zawartości wiadomości.
- `error` - informacja o błędzie wykonania operacji. Opis błędu znajduje się w zawartości wiadomości.
- `joined` - informacja o nawiązaniu połączenia z innym użytkownikiem, przekazując jako zawartość wiadomości jego nazwę.
- `disconnect` - informacja o odłączeniu się użytkownika o nazwie użytkownika równej tej w zawartości wiadomości. 
- `audio` - jak w sekcji powyżej, przekazanie wiadomości od jednego użytkownika do drugiego.
- `video` - jak w sekcji powyżej, przekazanie wiadomości od jednego użytkownika do drugiego.

Wiadomości wysyłane są za pomocą TCP, przez port 4201 (który można zmienić w pliku `src/constants.h`.

## Opis implementacji

### Implementacja serwera

Serwer napisany jest w języku C z wykorzystaniem BSD sockets oraz mechanizmów wielowątkowości. Posiada on mechanizm logowania wszystkich swoich wiadomości na standardowe wyjście (tzw. tryb debugowania). 
Działanie serwera podzielić można na 3 etapy:

- *Rozpoczęcie działania* - serwer wykorzystuje poszczególne funkcje z pliku `src/main_server.c` do zasetupowania się. Serwer przyjmuje dowolne adresy IP na port 4201. Tworzony jest socket, który wraz z opcjami REUSEADDR/REUSEPORT bindowany jest na urządzeniu oraz jest uruchamiany w trybie nasłuchiwania. Tworzy on również nagłówek do synchronizowalnej listy użytkowników (implementacje można znaleźć w pliku `src/structs/list_head.[hc]`).

- *Działanie serwera* -  działanie głównej pętli wątku serwera działa znajduje się w funkcji `ServerMainLoop` i `handleConnection` w pliku `src/main_server.c`. Serwer dla każdego użytkownika podłączonego tworzy strukturę User (`src/structures/user.[hc]`) która utrzymuje wszystkie informacje dotyczące użytkownika końcowego. W strukturze tej przechowywane są takie informacje jak np nazwa użytkownika, czy deskryptor połączenia z nim. Użytkownik umieszczany jest w liście, a następnie wszystkie operacje połączenia wykonywane są w osobnym, stworzonym dla niego wątku. Wątek wykonuje operacje odczytu na przekazanym mu deskryptorze do momentu, aż deskryptor ten nie zostanie zamknięty. Odebrana wiadomość kopiowana jest do końca bufora posiadającego znacznie większą objętość w porównaniu do bufora odczytującego wiadomości. Następnie rozdzielamy bufor przy wystąpieniu znaku separatora (domyślnie `\n`, ale można zmienić go w pliku `constants.h`, dzięki czemu mamy łatwy podział na poszczególne wiadomości, niewelując problem fragmentacji oraz sklejania. Z wiadomości wyciągany jest jej typ, który pozwala na wykonanie poszczególnych operacji. 

- *Zakmnięcie serwera* - przy zamknięciu serwera istotne są 2 aspekty. Po pierwsze zamknięcie wątku, w trakcie którego zamykany jest deskrytptor połączenia użytkownika oraz rozłączane jest połączenie z podłączonym do niego użytkownikiem (jeżeli takowy istnieje), a następnie usuwa struktury stworzone przez użytkownika i zamyka wątek. W przypadku zamknięcia głównego wątku serwera, wpierw następuje rozłączenie ze wszystkimi użytkownikami końcowymi, a następnie po ich usunięciu zamykany jest pierwotny port na którym wykonywane było nasłuchiwanie nowych połączeń, zwalniając w ten sposób wykorzystywany port. 

### Implementacja klienta

Klient jest napisany w Javie z wykorzystaniem JavaFX oraz JFoenix. Do budowania projektu wykorzystywane jest narzędzie Maven. Do komunikacji sieciowej wykorzystano Java Socket (opakowane przez PrintWriter i BufferedReader). 

Obsługa funkcji sieciowych realizowana jest w osobnych wątkach, żeby nie blokować interfejsu użytkownika. Klient wykorzystuje po jednym wątku dla:
- przechwytywania i wysyłania audio z mikrofonu, 
- przechwytywania i wysyłania obrazu z kamerki, 
- odbierania danych od servera i wykonywania związanych z nimi akcji. 

Dodatkowy wątek jest wykorzstywany podczas próby połączenia z serverem. Do przechwytywania i odtwarzania dzwięku użyto biblioteki javax.sound.sampled, a do obsługi kamerki biblioteki sarxos.webcam. 

Program uzyskuje dostęp do lini wejścia audio (poprzez własny bufor), a także kamerki i enkoduje odczytane dane przy pomocy Base64 encoding - dzięki temu upewniamy się, że specjalne znaki wykorzystywane w naszym protokole nie pojawią się w polu content. 

Tak przygotowane informacje są przesyłane jako "content" do servera, który zajmuje się przekierowaniem ich do odpowiedniego odbiorcy. 
Drugi klient podłączony do tej samej rozmowy odwraca proces dekodując dane i przesyłając dzwięk do wyjścia audio, a klatkę obrazu z kamerki do interfejsu użytkownika.
Żeby zminimalizować opóźnienie dzwięku powstające przez chwilowe rozłączenia, wątek odbiorczy sprawdza, czy wyjściowy bufor audio nie jest przypadkiem zapełniony. 
W razie potrzeby czyści bufor, co pozwala utrzymać opóźnienie dzwięku na wystarczającym poziomie, by prowadzić rozmowę. Bez tego mechanizmu każda sekunda, w której bufor wyjścia audio był pusty powodowałaby kaskadowo narastające opóźnienia dziwęku - dochodziłoby nawet do 30-sekundowego opóźnienia w transmisji audio. 
Wyłączenie kamerki jest realizowane przez zablokowanie semafora, z któego korzysta wątek odpowiedzialny za wysyłanie obrazu. 
Dzięki temu nie ma aktywnego czekania. Wyłącznie mikrofonu było prostsze, gdyż wystarczyło zatrzymać linię wejścia audio. 
Rozłączenie po stronie klienta jest realizowane przez zamknięcie socketu. Serwer reagując na to wysyła informację do drugiego klienta o zakończeniu rozmowy i dopiero potem zamyka połączenie.
