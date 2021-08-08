use actix_web::{App, get, HttpResponse, HttpServer, Responder, web};
use jsonpath_lib::{JsonSelector, PathParser};
use jsonpath_lib::{Selector, Parser};
use serde_json::{json, Value};

#[actix_web::main]
async fn main() -> std::io::Result<()> {

    let json: Value = json!(
        {
          "store": {
            "book": [
              {
                "category": "reference",
                "author": "Nigel Rees",
                "title": "Sayings of the Century",
                "price": 8.95
              },
              {
                "category": "fiction",
                "author": "Evelyn Waugh",
                "title": "Sword of Honour",
                "price": 12.99
              },
              {
                "category": "fiction",
                "author": "Herman Melville",
                "title": "Moby Dick",
                "isbn": "0-553-21311-3",
                "price": 8.99
              },
              {
                "category": "fiction",
                "author": "J. R. R. Tolkien",
                "title": "The Lord of the Rings",
                "isbn": "0-395-19395-8",
                "price": 22.99
              }
            ],
            "bicycle": {
              "color": "red",
              "price": 19.95
            }
          },
          "expensive": 10
        });

    let path = r#"$..book[ ?( (@.price < 13 || $.store.bicycle.price < @.price) && @.price <=10 ) ]"#;

    let json_data = web::Data::new(json);
    let path_data = web::Data::new(path);

    HttpServer::new(move || {
        App::new()
            .app_data(json_data.clone())
            .app_data(path_data.clone())
            .service(new)
            .service(old)
    })
        .bind("127.0.0.1:8080")?
        .run()
        .await
}

#[get("/new")]
async fn new(path_data: web::Data<&str>, json_data: web::Data<Value>) -> impl Responder {
    let parser = PathParser::compile(path_data.get_ref()).unwrap();
    let mut selector = JsonSelector::new(parser);
    selector.value(json_data.get_ref());
    let result = selector.select_as_str().unwrap();
    HttpResponse::Ok().body(result)
}

#[get("/old")]
async fn old(path_data: web::Data<&str>, json_data: web::Data<Value>) -> impl Responder {
    let mut selector = Selector::default();
    selector.str_path(path_data.get_ref());
    selector.value(json_data.get_ref());
    let result = selector.select_as_str().unwrap();
    HttpResponse::Ok().body(result)
}
