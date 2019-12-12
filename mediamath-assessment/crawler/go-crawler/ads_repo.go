package go_crawler

import (
	"context"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"time"
)

type AdsRepo interface {
	Save(ads *Ads) error
	GetByPublisherName(publisherName string) (*Ads, error)
}

type RecordMongoRepo struct {
	Collection *mongo.Collection
}

func (mongo RecordMongoRepo) Save(ads *Ads) error {
	filter := bson.M{"publisher": ads.Publisher}
	update := bson.M{"$set": bson.M{"records": ads.Records}}
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	_, err := mongo.Collection.UpdateOne(ctx, filter, update, options.Update().SetUpsert(true))
	return err
}

func (mongo RecordMongoRepo) GetByPublisherName(publisherName string) (*Ads, error) {
	filter := bson.M{"publisher": publisherName}
	var result Ads
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	err := mongo.Collection.FindOne(ctx, filter).Decode(&result)
	if err != nil {
		return nil, err
	}
	return &result, nil
}
