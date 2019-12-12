package main

import (
	"context"
	go_crawler "crawler/go-crawler"
	"encoding/json"
	"fmt"
	"github.com/gorilla/mux"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"log"
	"net/http"
	"sync"
)

var publisherUrls = []string{
	"http://www.cnn.com/ads.txt",
	"http://www.gizmodo.com/ads.txt",
	"http://www.nytimes.com/ads.txt",
	"https://www.bloomberg.com/ads.txt",
	"https://wordpress.com/ads.txt",
}

// MongoDB setup
var clientOptions = options.Client().ApplyURI("mongodb://localhost:27017")
var client, _ = mongo.Connect(context.TODO(), clientOptions)
var collection = client.Database("crawler").Collection("ads")
var repo = go_crawler.RecordMongoRepo{Collection: collection}

func crawl(w http.ResponseWriter, r *http.Request) {
	fmt.Printf("Crawlign started")
	var wg sync.WaitGroup
	wg.Add(len(publisherUrls))

	crawl0 := func(url string, wg *sync.WaitGroup) {
		fmt.Printf("Crawling: %s ...\n", url)
		ads, err := go_crawler.Crawl(url)
		if err != nil {
			log.Fatal(err)
		}
		err = repo.Save(ads)
		if err != nil {
			log.Fatal(err) // todo use a separate chanel to report errors
		}
		wg.Done()
	}

	for _, url := range publisherUrls {
		go crawl0(url, &wg)
	}

	wg.Wait()
	_, err := fmt.Fprintf(w, "Crawling has been successfuly completed")
	if err != nil {
		log.Fatal(err)
	}

}

func getByPublisher(w http.ResponseWriter, r *http.Request) {
	publisherName := mux.Vars(r)["publisher"]
	ads, _ := repo.GetByPublisherName(publisherName)
	_ = json.NewEncoder(w).Encode(ads.Records) // todo send 404 if there is no such publisher in database
}

func main() {
	address := "localhost:8080"
	fmt.Printf("Server started on %s\n", address)
	router := mux.NewRouter().StrictSlash(true)
	router.HandleFunc("/crawl", crawl) // todo consider set http method to POST
	router.HandleFunc("/ads/{publisher}", getByPublisher)
	log.Fatal(http.ListenAndServe(address, router))
}
