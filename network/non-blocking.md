# Неблокирующий сервер

## Введение

Потоки, блокировки, потребление памяти.


## Дизайн

### Общая логика работы сервера


### Приём новых подключений


### Автомат отдельного подключения


## Реализация



## Тестирование

### Ручное тестирование

```sh
$ mkdir /tmp/nb-test
$ cd /tmp/nb-test
$ head -c 1024 /dev/urandom >data.bin
$ cat data.bin | nc 127.0.0.1 8080
```


### Нагрузочное тестирование

[tcpkali](https://github.com/satori-com/tcpkali)

```sh
$ tcpkali -f data.bin -c 10 -T 5s 127.0.0.1:8080
```


## Заключение

Дальнейшие улучшения:
* произвольная обработка данных с помощью паттерна "стратегия";