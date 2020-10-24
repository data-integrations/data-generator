# Data Generator Source

## Description
This source generates fake data to use in test scenarios.
It can be configured to generate a few pre-canned datasets with pre-defined schemas
with various knobs to control data skew and nullability. It can also be configured in
a more advanced mode with more control over the output schema and data generated for
each field.

## Common Properties
**Reference Name:** Name used to uniquely identify this source for lineage, annotating metadata, etc.

**Number of Splits:** Number of splits to generate. This controls the level of parallelism.

**Records per Split:** Number of records to generate for each split. Multiply this number by
the number of splits to get the total number of output records.

**Random Seed:** Optional seed to use for random data generator. Set this if you want your
"random" data to be generated in the same way each time.

**Dataset:** Type of dataset to output. Allows selecting a pre-canned dataset with a pre-defined
output schema and set of properties. A 'custom' dataset can also be chosen, which requires
defining how all the output fields should be generated.

## Users Dataset
The 'users' dataset outputs data with the following schema:

| name       | type   | description                                                    |
|------------|--------|----------------------------------------------------------------|
| id         | long   | sequential number from 1 to total # of records                 |
| first_name | string | first name                                                     |
| last_name  | string | last name                                                      |
| email      | string | email address                                                  |
| phone      | string | phone number                                                   |
| profession | string | job profession                                                 |
| age        | int    | age from 18 to 100                                             |
| address    | string | home address                                                   |
| score      | double | gaussian distribution from 0 to 100                            |
| desc       | string | random characters used to control record size                  |
| payload    |        | random bytes or semi-random string used to control record size |

Every field except the id and payload can be made nullable.
The dataset can be configured with the following properties:

**Chance to Generate a Skewed ID:** Rough percentage of data that should use a skewed ID.
Unskewed data will generate sequential numbers from 1 to the total number of records.
Skewed data will be a randomly generated number from 1 to the number of skewed IDs,
each with equal probability.

**Number of Skewed IDs:** Number of skewed IDs to generate. Skewed IDs start from 1 and
go up to this number.

**Length (# characters) of Description Field:** Number of characters of the desc field. This is used to
control roughly how large each record should be.

**Size (KB) of Payload Field:** Size in kilobytes of the payload field. This is used to
control roughly how large each record should be.

**Payload Type:** Type of payload to generate. Can be random bytes or a semi-random string.
Use random bytes if you don't want your records to be compressible. Use a semi-random string
if you do want them to be compressible.

**Chance to Generate Null Values:** For every field except id and payload, the chance
that the value should be null. 

## Purchases Dataset
The 'purchases' dataset outputs data with the following schema:

| name         | type      | description                                                    |
|--------------|-----------|----------------------------------------------------------------|
| id           | string    | UUID representing the purchase id                              |
| user_id      | long      | ID of the user that made the purchase                          |
| ts           | timestamp | timestamp of the purchase, from 2020-01-01 to 2020-04-01       |
| price        | int       | random number of cents for the purchase, from 99 to 10000      |
| desc         | string    | random characters used to control record size                  |
| payload      |           | random bytes or semi-random string used to control record size |

The 'ts' and 'price' fields are nullable, but everything else is not.
The dataset can be configured with the following properties:

**Chance to Generate a Skewed User ID:** Rough percentage of data that should use a skewed user ID.
Unskewed data will be randomly generated number from 1 to the total number of records, each
with equal probability. Skewed data will be a randomly generated number from 1 to the
number of skewed IDs, each with equal probability

**Number of Skewed User IDs:** Number of skewed user IDs to generate. Skewed IDs start from 1 and
go up to this number.

**Length of Description Field:** Number of characters of the desc field.

**Size (KB) of Payload Field:** Size in kilobytes of the payload field. This is used to
control roughly how large each record should be.

**Payload Type:** Type of payload to generate. Can be random bytes or a semi-random string.
Use random bytes if you don't want your records to be compressible. Use a semi-random string
if you do want them to be compressible.

**Chance to Generate Null Values:** For the 'ts' and 'price' fields, the chance
that the value should be null. 

## Custom Dataset
A custom dataset can be defined by specifying which fields should be generated and how they
should be generated. This is done by providing a JSON field specification.
The specification is of the following format:

```
{
  "fields": [
    {
      "name": <field name>,
      "nullChance": <0 - 100>,
      "type": <generator type>,
      "args": { ... }
    }
  ]
}
```

Each generator type defines its own set of arguments that it supports. For example, the 'GAUSSIAN' type
allows passing in 'mean' and 'stddev' arguments, which would be specified as:

```
{
  "fields": [
    {
      "name": "score",
      "nullChance": 0,
      "type": "GAUSSIAN",
      "args": {
        "mean": 0.0,
        "stddev": 100.0
      }
    }
  ]
}
```


### Generator Types

| type                   | schema    | description                                                         |
|------------------------|-----------|---------------------------------------------------------------------|
| ADDRESS                | string    | random full address or address components                           |
| CREDIT_CARD            | string    | random credit card numbers                                          |
| EMAIL                  | string    | random email addresses                                              |
| GAUSSIAN               | double    | numbers in a gaussian distribution                                  |
| LOREM                  | string    | random sentences of a given character length                        |
| NAME                   | string    | random full names or name parts                                     |
| PHONE_NUMBER           | string    | random phone numbers                                                |
| PROFESSION             | string    | random job professions                                              |
| RANDOM_BYTES           | bytes     | random byte arrays of a specific size                               |
| RANDOM_INT             | int       | random integers in a range with a uniform distribution              |
| RANDOM_INT_SKEWED      | int       | RANDOM_INT except a subset of numbers is more likely                |
| RANDOM_LONG            | long      | random longs in a range with a uniform distribution                 |
| RANDOM_LONG_SKEWED     | long      | RANDOM_LONG except a subset of numbers is more likely               |
| SEMI_RANDOM_STRING     | string    | generates compressible strings from pre-defined values              |
| SEQUENTIAL_INT         | int       | integers in a regularly increasing sequence                         |
| SEQUENTIAL_INT_SKEWED  | int       | SEQUENTIAL_INT except the next number in the sequence is sometimes  |
|                        |           | replaced with a number randomly chosen from a smaller subset        |
| SEQUENTIAL_LONG        | long      | longs in a regularly increasing sequence                            |
| SEQUENTIAL_LONG_SKEWED | long      | SEQUENTIAL_LONG except the next number in the sequence is sometimes |
|                        |           | replaced with a number randomly chosen from a smaller subset        |
| TEXT                   | string    | constant / static text                                              |
| TIMESTAMP              | timestamp | uniformly distributed timestamps between a start and end time       |
| UUID                   | string    | random UUID                                                         |

### Generator Arguments
Generator types that support arguments are described below.

#### ADDRESS

**type:** One of 'full', 'street', 'city', 'zip', 'state', 'country', 'latitude', or 'longitude'.
Defaults to 'full'.

#### GAUSSIAN

**mean:** Mean of the distribution as a double.

**stddev:** Standard deviation of the distribution as a double.

#### LOREM

**size:** Number of characters in the string. Defaults to 100.

#### NAME

**type:** One of 'full', 'first', 'last', 'username'. Defaults to 'full'.

#### RANDOM_BYTES

**size:** Size of the byte array to generate. Defaults to 1024.

#### RANDOM_INT

**min:** Minimum (inclusive) integer to generate. Defaults to 0.

**max:** Maximum (inclusive) integer to generate. Defaults to the maximum integer value (2,147,483,647).

#### RANDOM_INT_SKEWED

**min:** Minimum (inclusive) integer to generate. Defaults to 0.

**max:** Maximum (inclusive) integer to generate.
If no value is given, it defaults to the maximum integer value (2,147,483,647).

**skewChance:** Percent chance to generate a skewed number. Must be between 0 and 100. Defaults to 10.

**skewMin:** Minimum (inclusive) integer to generate when generating skewed data. Defaults to 0.

**skewMax:** Minimum (inclusive) integer to generate when generating skewed data. Defaults to 10.

#### RANDOM_LONG

**min:** Minimum (inclusive) long to generate. Defaults to 0.

**max:** Maximum (inclusive) long to generate. Defaults to the maximum long value (2^63-1).

#### RANDOM_LONG_SKEWED

**min:** Minimum (inclusive) long to generate. Defaults to 0.

**max:** Maximum (inclusive) long to generate. Defaults to the maximum long value (2^63-1).

**skewChance:** Percent chance to generate a skewed number. Must be between 0 and 100. Defaults to 10.

**skewMin:** Minimum (inclusive) integer to generate when generating skewed data. Defaults to 0.

**skewMax:** Minimum (inclusive) integer to generate when generating skewed data. Defaults to 10.

#### SEMI_RANDOM_STRING

**size:** Number of characters in the string. Defaults to 100.

#### SEQUENTIAL_INT

**start:** Number to start at. Defaults to 0.

**step:** Amount to increase the number by when generating the next number in the sequence. Defaults to 1.

#### SEQUENTIAL_INT_SKEWED

**start:** Number to start at. Defaults to 0.

**step:** Amount to increase the number by when generating the next number in the sequence. Defaults to 1.

**skewChance:** Percent chance to generate a skewed number. Must be between 0 and 100.
When a skewed number is generated, the number that would otherwise be next in the sequence is skipped.
Defaults to 10.

**skewMin:** Minimum (inclusive) integer to generate when generating skewed data. Defaults to 0.

**skewMax:** Minimum (inclusive) integer to generate when generating skewed data. Defaults to 10.

#### SEQUENTIAL_LONG

**start:** Number to start at. Defaults to 0.

**step:** Amount to increase the number by when generating the next number in the sequence. Defaults to 1.

#### SEQUENTIAL_LONG_SKEWED

**start:** Number to start at. Defaults to 0.

**step:** Amount to increase the number by when generating the next number in the sequence. Defaults to 1.

**skewChance:** Percent chance to generate a skewed number. Must be between 0 and 100.
When a skewed number is generated, the number that would otherwise be next in the sequence is skipped.
Defaults to 10.

**skewMin:** Minimum (inclusive) integer to generate when generating skewed data. Defaults to 0.

**skewMax:** Minimum (inclusive) integer to generate when generating skewed data. Defaults to 10.

#### TEXT

**text:** Text to insert into the field.  Defaults to empty text.

#### TIMESTAMP

**from:** Minimum timestamp in milliseconds to generate. Defaults to 0.

**to:** Maximum timestamp in milliseconds to generate.
Defaults to the current time, which can be different for each data split.

