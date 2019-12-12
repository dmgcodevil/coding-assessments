package go_crawler

import (
	"encoding/json"
	"errors"
	"io/ioutil"
	"net/http"
	"net/url"
	"strings"
)
import "fmt"

type AccountType int

const (
	DIRECT AccountType = iota
	RESELLER
)

var accountTypeNames = []string{
	"DIRECT",
	"RESELLER"}

func (at AccountType) String() string {
	return accountTypeNames[at]
}

type Record struct {
	DomainName  string      `json:"domainName"`
	AccountId   string      `json:"accountId"`
	AccountType AccountType `json:"accountType"`
	AuthorityId string      `json:"authorityId"` // optional, i.e. may be empty
}

func (r *Record) MarshalJSON() ([]byte, error) {
	type Alias Record
	return json.Marshal(&struct {
		AccountType string `json:"accountType"`
		*Alias
	}{
		AccountType: r.AccountType.String(),
		Alias:       (*Alias)(r),
	})
}

type Ads struct {
	Publisher string
	Records   []Record
}

var accountTypes = map[string]AccountType{
	"DIRECT":   DIRECT,
	"RESELLER": RESELLER,
}

var invalidCharacters = []string{" ", "\t", ","}

func Crawl(urlString string) (*Ads, error) {
	u, err := url.Parse(urlString)
	if err != nil {
		return nil, err
	}
	publisher := u.Hostname()

	resp, err := http.Get(urlString)
	if err != nil {
		return nil, err
	}

	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)

	if err != nil {
		return nil, err
	}

	lines := Split(string(body))
	records := parseRecords(lines)

	return &Ads{
		Publisher: publisher,
		Records:   records}, nil
}

// converts a  raw string to AccountType
// note: this function is case sensitive
func ParseAccountType(str string) (*AccountType, error) {
	val, ok := accountTypes[str]
	if !ok {
		return nil, errors.New(fmt.Sprintf("%s isn't valid accout type", str))
	}
	return &val, nil
}

// splits the input by an end-of-line marker, i.e. CR, CRLF
func Split(input string) []string {
	return strings.Split(strings.Replace(input, "\r\n", "\n", -1), "\n")
}

// parses records from raw lines. malformed records ignored
func parseRecords(lines []string) []Record {
	var records []Record
	for _, line := range lines {
		record, err := parseRecord(line)
		if err == nil {
			records = append(records, *record)
		}
	}

	return records
}

func parseRecord(input string) (*Record, error) {
	input = deleteComment(input)

	parts := strings.Split(input, ",")
	if len(parts) < 3 || len(parts) > 4 {
		return nil, errors.New(fmt.Sprintf("invalid line: %s", input))
	}

	for i := 0; i < len(parts); i++ {
		parts[i] = normalize(parts[i])
	}

	errorMsgTemplate := "Invalid %s. Line: %s"

	domainName := parts[0]
	if !isValid(domainName) {
		return nil, errors.New(fmt.Sprintf(errorMsgTemplate, "domain name", input))
	}

	accountId := parts[1]
	if !isValid(accountId) {
		return nil, errors.New(fmt.Sprintf(errorMsgTemplate, "account id", input))
	}

	accountType, err := ParseAccountType(parts[2])
	if err != nil {
		return nil, errors.New(fmt.Sprintf(errorMsgTemplate, "account type", input))
	}

	var authorityId = "" // optional

	if len(parts) == 4 {
		authorityId = parts[3]
		if !isValid(authorityId) {
			return nil, errors.New(fmt.Sprintf(errorMsgTemplate, "authority Id", input))
		}

	}

	return &Record{
		DomainName:  domainName,
		AccountId:   accountId,
		AccountType: *accountType,
		AuthorityId: authorityId,
	}, nil

}

func deleteComment(str string) string {
	commentPos := strings.Index(str, "#")
	if commentPos != -1 {
		return str[0:commentPos]
	}
	return str
}

// normalizes the given string
func normalize(str string) string {
	return strings.TrimSpace(str)
}

func isValid(str string) bool {
	for _, value := range invalidCharacters {
		if len(str) == 0 || strings.Contains(str, value) {
			return false
		}
	}
	return true
}
