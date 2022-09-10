package controlwork;

import com.sun.net.httpserver.HttpExchange;
import server.BasicServer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import server.ContentType;
import server.ResponseCodes;
import server.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Control extends BasicServer {
    private static CalendarModel calendarModel = new CalendarModel();
    private final static Configuration freemarker = initFreeMarker();

    public Control(String host, int port) throws IOException {
        super(host, port);
        registerGet("/", this::calendarHandler);
        registerGet("/tasks", this::tasksHandler);
        registerGet("/task/add", this::taskAddHandler);
        registerPost("/task/add", this::taskAddPostHandler);
        registerGet("/task/delete", this::taskDeleteGetHandler);
        registerGet("/task", this::taskHandler);
        registerPost("/task", this::taskChangePostHandler);
    }

    private void taskChangePostHandler(HttpExchange exchange) {
        String raw = getBody(exchange);
        Map<String, String> parsed = Utils.parseUrlEncoded(raw, "&");
        for (int i = 0; i < calendarModel.getDays().size(); i++){
            if(parsed.get("day").equalsIgnoreCase(calendarModel.getDays().get(i).getDate().toString())){
                for (int j = 0; j < calendarModel.getDays().get(i).getTasks().size(); j++){
                    if (parsed.get("taskId").equalsIgnoreCase(calendarModel.getDays().get(i).getTasks().get(j).getTaskId())){
                        calendarModel.getDays().get(i).getTasks().set(j, new Task(parsed.get("taskId"), parsed.get("name"), parsed.get("description"), Arrays.stream(TypeOfTask.values()).filter(e -> e.getName().equalsIgnoreCase(parsed.get("selected"))).findFirst().get()));
                        break;
                    }
                }
                break;
            }
        }
        CalendarModel.writeFile(calendarModel.getDays());
        redirect303(exchange, "/tasks?date=" + parsed.get("day"));
    }

    private void taskHandler(HttpExchange exchange) {
        Map<String,Object> data = new HashMap<>();
        String queryParams = getQueryParams(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(queryParams, "&");
        Task task = new Task();

        for (var day: calendarModel.getDays()) {
            if(params.get("date").equalsIgnoreCase(day.getDate().toString())){
                for (int j = 0; j < day.getTasks().size(); j++){
                    if (params.get("taskId").equalsIgnoreCase(day.getTasks().get(j).getTaskId())){
                        task = day.getTasks().get(j);
                        data.put("day", day);
                        data.put("task", task);
                        data.put("types", TypeOfTask.values());
                        break;
                    }
                }
                break;
            }
        }
        renderTemplate(exchange, "task.html", data);
    }

    private void taskDeleteGetHandler(HttpExchange exchange) {
        String queryParams = getQueryParams(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(queryParams, "&");
        for (int i = 0; i < calendarModel.getDays().size(); i++){
            if(params.get("date").equalsIgnoreCase(calendarModel.getDays().get(i).getDate().toString())){
                for (int j = 0; j < calendarModel.getDays().get(i).getTasks().size(); j++){
                    if (params.get("taskId").equalsIgnoreCase(calendarModel.getDays().get(i).getTasks().get(j).getTaskId())){
                        calendarModel.getDays().get(i).getTasks().remove(j);
                        break;
                    }
                }
                break;
            }
        }
        CalendarModel.writeFile(calendarModel.getDays());
        redirect303(exchange, "/tasks?date=" + params.get("date"));
    }

    private void taskAddPostHandler(HttpExchange exchange) {
        String raw = getBody(exchange);
        Map<String, String> parsed = Utils.parseUrlEncoded(raw, "&");
        for (int i = 0; i < calendarModel.getDays().size(); i++){
            if(parsed.get("day").equalsIgnoreCase(calendarModel.getDays().get(i).getDate().toString())){
                calendarModel.getDays().get(i).getTasks().add(new Task(parsed.get("taskId"), parsed.get("name"), parsed.get("description"), Arrays.stream(TypeOfTask.values()).filter(e -> e.getName().equalsIgnoreCase(parsed.get("selected"))).findFirst().get()));
                break;
            }
        }
        CalendarModel.writeFile(calendarModel.getDays());
        redirect303(exchange, "/tasks?date=" + parsed.get("day"));
    }

    private void taskAddHandler(HttpExchange exchange) {
        Map<String,Object> data = new HashMap<>();
        String queryParams = getQueryParams(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(queryParams, "&");
        Task task = new Task();

        for (var day: calendarModel.getDays()) {
            if(params.get("date").equalsIgnoreCase(day.getDate().toString())){
                data.put("day", day);
                data.put("task", task);
                data.put("types", TypeOfTask.values());
                break;
            }
        }
        renderTemplate(exchange, "add.html", data);
    }

    private void tasksHandler(HttpExchange exchange) {
        String queryParams = getQueryParams(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(queryParams, "&");
        Day currentDay = new Day();
        for (var day: calendarModel.getDays()) {
            if(params.get("date").equalsIgnoreCase(day.getDate().toString())){
                currentDay = day;
            }
        }
        renderTemplate(exchange, "tasks.html", currentDay);
    }

    private void calendarHandler(HttpExchange exchange) {
        renderTemplate(exchange, "index.html", new CalendarModel());
    }

    private static Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
            // путь к каталогу в котором у нас хранятся шаблоны
            // это может быть совершенно другой путь, чем тот, откуда сервер берёт файлы
            // которые отправляет пользователю
            cfg.setDirectoryForTemplateLoading(new File("data"));

            // прочие стандартные настройки о них читать тут
            // https://freemarker.apache.org/docs/pgui_quickstart_createconfiguration.html
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
        try {
            // загружаем шаблон из файла по имени.
            // шаблон должен находится по пути, указанном в конфигурации
            Template temp = freemarker.getTemplate(templateFile);

            // freemarker записывает преобразованный шаблон в объект класса writer
            // а наш сервер отправляет клиенту массивы байт
            // по этому нам надо сделать "мост" между этими двумя системами

            // создаём поток который сохраняет всё, что в него будет записано в байтовый массив
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // создаём объект, который умеет писать в поток и который подходит для freemarker
            try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {

                // обрабатываем шаблон заполняя его данными из модели
                // и записываем результат в объект "записи"
                temp.process(dataModel, writer);
                writer.flush();

                // получаем байтовый поток
                var data = stream.toByteArray();

                // отправляем результат клиенту
                sendByteData(exchange, ResponseCodes.OK, ContentType.TEXT_HTML, data);
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }
}
