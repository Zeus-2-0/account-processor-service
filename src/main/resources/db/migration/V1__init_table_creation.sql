DROP TABLE IF EXISTS `accountprocessordb`.`broker`;
DROP TABLE IF EXISTS `accountprocessordb`.`sponsor`;
DROP TABLE IF EXISTS `accountprocessordb`.`payer`;
DROP TABLE IF EXISTS `accountprocessordb`.`member_language`;
DROP TABLE IF EXISTS `accountprocessordb`.`member_identifier`;
DROP TABLE IF EXISTS `accountprocessordb`.`member_phone`;
DROP TABLE IF EXISTS `accountprocessordb`.`member_address`;
DROP TABLE IF EXISTS `accountprocessordb`.`member_email`;
DROP TABLE IF EXISTS `accountprocessordb`.`alternate_contact`;
DROP TABLE IF EXISTS `accountprocessordb`.`member_premium`;
DROP TABLE IF EXISTS `accountprocessordb`.`premium_span`;
DROP TABLE IF EXISTS `accountprocessordb`.`enrollment_span`;
DROP TABLE IF EXISTS `accountprocessordb`.`member`;
DROP TABLE IF EXISTS `accountprocessordb`.`account`;
DROP TABLE IF EXISTS `accountprocessordb`.`transaction`;
DROP TABLE IF EXISTS `accountprocessordb`.`payload_tracker_detail`;
DROP TABLE IF EXISTS `accountprocessordb`.`payload_tracker`;
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`transaction` (
    `transaction_sk` VARCHAR(36) NOT NULL COMMENT 'The primary key of the table',
    `ztcn` VARCHAR(50) NOT NULL COMMENT 'The zeus transaction control number',
    `zfcn` VARCHAR(50) NULL COMMENT 'The zeus file control number',
    `transaction_received_date` DATETIME NOT NULL COMMENT 'The date when the transaction was received',
    `transaction_source_type_code` VARCHAR(50) NOT NULL COMMENT 'The source of the transaction',
    `created_date` DATETIME NULL COMMENT 'The date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'The date when the record was updated',
    PRIMARY KEY (`transaction_sk`, `ztcn`))
    ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`account` (
    `account_sk` VARCHAR(36) NOT NULL COMMENT 'Primary key of the table',
    `transaction_sk` VARCHAR(36) NOT NULL COMMENT 'The transaction for which the match was found',
    `match_found` BOOLEAN NOT NULL COMMENT 'Indicates if a match was found or not for the transaction',
    `match_account_sk` VARCHAR(36) NULL COMMENT 'The account sk of the matched account, this will be NULL if no match was found',
    `account_number` VARCHAR(50) NOT NULL COMMENT 'The account number for the account that was matched or a new account number for the account to be created',
    `line_of_business_type_code` VARCHAR(50) NOT NULL,
    `created_date` DATETIME NULL,
    `updated_date` DATETIME NULL,
    PRIMARY KEY (`account_sk`),
    INDEX `acct_trans_fk_idx` (`transaction_sk` ASC) VISIBLE,
    CONSTRAINT `acct_trans_fk`
    FOREIGN KEY (`transaction_sk`)
    REFERENCES `accountprocessordb`.`transaction` (`transaction_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'This table indicates if an account match was found for the transaction or not';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`enrollment_span` (
    `enrollment_span_sk` VARCHAR(36) NOT NULL COMMENT 'Primary key of the table',
    `acct_enrollment_span_sk` VARCHAR(36) NULL COMMENT 'This will be populated only for enrollment span that were already present for the account before the transaction was received.',
    `enrollment_span_code` VARCHAR(50) NOT NULL COMMENT 'The enrollment span code associated with the enrollment span',
    `ztcn` VARCHAR(20) NOT NULL COMMENT 'The transaction control number that created the enrollment span',
    `account_sk` VARCHAR(36) NOT NULL COMMENT 'The account for which the enrollment span is associated',
    `state_type_code` VARCHAR(50) NOT NULL COMMENT 'The state for which the enrollment span is created',
    `marketplace_type_code` VARCHAR(45) NOT NULL COMMENT 'The marketplace for which the enrollment span is created',
    `business_unit_type_code` VARCHAR(50) NOT NULL COMMENT 'The business unit for which the enrollment span is created',
    `coverage_type_code` VARCHAR(50) NOT NULL COMMENT 'The coverage type associated with the enrollment span',
    `start_date` DATE NOT NULL COMMENT 'The start date of the enrollment span',
    `end_date` DATE NOT NULL COMMENT 'The end date of the enrollment span',
    `exchange_subscriber_id` VARCHAR(50) NOT NULL COMMENT 'The exchange subscriber id for which the enrollment span is created',
    `effectuation_date` DATE NULL COMMENT 'The effectuation date of the enrollment span',
    `plan_id` VARCHAR(100) NOT NULL COMMENT 'The QHP Id of the enrollment span',
    `product_type_code` VARCHAR(100) NOT NULL COMMENT 'The product type of the plan',
    `group_policy_id` VARCHAR(100) NOT NULL COMMENT 'The group policy id of the enrollment span',
    `delinq_ind` BOOLEAN NOT NULL DEFAULT 0 COMMENT 'Identifies if the enrollment span is delinquent',
    `paid_through_date` DATE NULL,
    `claim_paid_through_date` DATE NULL COMMENT 'The claim paid through date associated with the enrollment span',
    `status_type_code` VARCHAR(50) NOT NULL COMMENT 'The status of the enrollment span',
    `effective_reason` VARCHAR(150) NULL COMMENT 'The effective reason of the enrollment span',
    `term_reason` VARCHAR(150) NULL COMMENT 'The term reason of the enrollment span',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'The date when the record is created',
    `updated_date` DATETIME NULL COMMENT 'The date when the record is updated',
    PRIMARY KEY (`enrollment_span_sk`),
    INDEX `trans_acct_fk_idx` (`account_sk` ASC) VISIBLE,
    CONSTRAINT `trans_acct_fk`
    FOREIGN KEY (`account_sk`)
    REFERENCES `accountprocessordb`.`account` (`account_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'The enrollment span that are used or updated during the processing of the transaction for the account';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`premium_span` (
    `premium_span_sk` VARCHAR(36) NOT NULL,
    `enrollment_span_sk` VARCHAR(36) NOT NULL,
    `acct_premium_span_sk` VARCHAR(36) NULL COMMENT 'The premium span sk if the premium span already exist for the enrollment span.',
    `ztcn` VARCHAR(20) NOT NULL COMMENT 'The transaction control number that created the premium span',
    `premium_span_code` VARCHAR(50) NOT NULL COMMENT 'Unique code created for each of the premium span',
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `status_type_code` VARCHAR(50) NOT NULL COMMENT 'The status of the premium span',
    `csr_variant` VARCHAR(10) NOT NULL,
    `total_prem_amt` DECIMAL(10,2) NOT NULL COMMENT 'The total premium amount per month for the plan chosen by the member',
    `total_resp_amt` DECIMAL(10,2) NOT NULL COMMENT 'Total amount that the member is responsible for payment towards the premium',
    `aptc_amt` DECIMAL(10,2) NULL COMMENT 'Federal contribution towards the premium',
    `other_pay_amt` DECIMAL(10,2) NULL COMMENT 'The amounts contributed by other sources (like the state) towards the premium',
    `csr_amt` DECIMAL(10,2) NULL COMMENT 'The Cost Sharing Reduction amount',
    `sequence` INT NOT NULL COMMENT 'The sequence in which the premium span is created',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'Date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'Date when the record was updated',
    PRIMARY KEY (`premium_span_sk`),
    INDEX `enrollment_fk_idx` (`enrollment_span_sk` ASC) VISIBLE,
    CONSTRAINT `enrollment_fk`
    FOREIGN KEY (`enrollment_span_sk`)
    REFERENCES `accountprocessordb`.`enrollment_span` (`enrollment_span_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'The premium spans associated with the enrollment span';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`broker` (
    `broker_sk` VARCHAR(36) NOT NULL COMMENT 'The primary key of the table',
    `acct_broker_sk` VARCHAR(36) NULL COMMENT 'The key of the broker in MMS',
    `account_sk` VARCHAR(36) NOT NULL COMMENT 'The account that the broker is associated',
    `broker_code` VARCHAR(50) NOT NULL COMMENT 'The unique code of the broker',
    `broker_name` VARCHAR(100) NOT NULL COMMENT 'The name of the broker',
    `broker_id` VARCHAR(50) NOT NULL COMMENT 'The id of the broker',
    `agency_name` VARCHAR(100) NULL COMMENT 'The name of the agency',
    `agency_id` VARCHAR(50) NULL COMMENT 'The id of the agency',
    `account_number_1` VARCHAR(50) NULL COMMENT 'The first account number',
    `account_number_2` VARCHAR(50) NULL COMMENT 'The second account number',
    `start_date` DATE NOT NULL COMMENT 'The start date of the broker',
    `end_date` DATE NULL COMMENT 'The end date of the broker',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'The date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'The date when the record was updated',
    PRIMARY KEY (`broker_sk`),
    INDEX `acct_broker_fk_idx` (`account_sk` ASC) VISIBLE,
    CONSTRAINT `acct_broker_fk`
    FOREIGN KEY (`account_sk`)
    REFERENCES `accountprocessordb`.`account` (`account_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'The broker details of the account';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`sponsor` (
    `sponsor_sk` VARCHAR(36) NOT NULL COMMENT 'The primary key of the table',
    `acct_sponsor_sk` VARCHAR(36) NULL COMMENT 'The key of the sponsor in MMS',
    `account_sk` VARCHAR(36) NOT NULL COMMENT 'The account that the sponsor is associated',
    `sponsor_code` VARCHAR(50) NOT NULL COMMENT 'The unique code of the sponsor',
    `sponsor_name` VARCHAR(100) NOT NULL COMMENT 'The name of the sponsor',
    `sponsor_id` VARCHAR(50) NOT NULL COMMENT 'The id of the sponsor',
    `start_date` DATE NOT NULL COMMENT 'The start date of the sponsor',
    `end_date` DATE NULL COMMENT 'The end date of the sponsor',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'The date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'The date when the record was updated',
    PRIMARY KEY (`sponsor_sk`),
    INDEX `acct_sponsor_fk_idx` (`account_sk` ASC) VISIBLE,
    CONSTRAINT `acct_sponsor_fk`
    FOREIGN KEY (`account_sk`)
    REFERENCES `accountprocessordb`.`account` (`account_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'The sponsor details of the account';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`payer` (
    `payer_sk` VARCHAR(36) NOT NULL COMMENT 'The primary key of the table',
    `acct_payer_sk` VARCHAR(36) NULL COMMENT 'The key for the payer in MMS',
    `account_sk` VARCHAR(36) NOT NULL COMMENT 'The account to which the payer is associated',
    `payer_code` VARCHAR(50) NOT NULL COMMENT 'The unique code for the payer',
    `payer_name` VARCHAR(100) NOT NULL COMMENT 'The name of the payer',
    `payer_id` VARCHAR(50) NOT NULL COMMENT 'The id of the payer',
    `start_date` DATE NOT NULL COMMENT 'The start date of the payer',
    `end_date` DATE NULL COMMENT 'The end date of the payer',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'The date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'The date when the record was updated',
    PRIMARY KEY (`payer_sk`),
    INDEX `acct_payer_fk_idx` (`account_sk` ASC) VISIBLE,
    CONSTRAINT `acct_payer_fk`
    FOREIGN KEY (`account_sk`)
    REFERENCES `accountprocessordb`.`account` (`account_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'The payer of the account';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`member` (
    `member_sk` VARCHAR(36) NOT NULL COMMENT 'Primary key of the table',
    `account_sk` VARCHAR(36) NOT NULL COMMENT 'The foreign key connects the member with the account',
    `acct_member_sk` VARCHAR(36) NULL COMMENT 'The key that is assigned to the member in MMS. This will be NULL if the member is not present in the MMS',
    `trans_member_code` VARCHAR(50) NULL COMMENT 'The unique code for the member by Transaction manager',
    `member_code` VARCHAR(50) NOT NULL,
    `relationship_type_code` VARCHAR(50) NOT NULL,
    `first_name` VARCHAR(100) NOT NULL,
    `middle_name` VARCHAR(50) NULL,
    `last_name` VARCHAR(100) NOT NULL,
    `date_of_birth` DATE NULL,
    `gender_type_code` VARCHAR(20) NULL,
    `height` DECIMAL(10,2) NULL COMMENT 'The height of the member',
    `weight` DECIMAL(10,2) NULL COMMENT 'The weight of the member',
    `tobacco_ind` BOOLEAN NULL,
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL,
    `updated_date` DATETIME NULL,
    PRIMARY KEY (`member_sk`),
    INDEX `member_acct_fk_idx` (`account_sk` ASC) VISIBLE,
    CONSTRAINT `member_acct_fk`
    FOREIGN KEY (`account_sk`)
    REFERENCES `accountprocessordb`.`account` (`account_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`member_premium` (
    `member_premium_sk` VARCHAR(36) NOT NULL COMMENT 'The primary key of the table',
    `acct_mem_prem_sk` VARCHAR(36) NULL COMMENT 'The key of the member premium record in MMS',
    `acct_prem_span_sk` VARCHAR(36) NULL COMMENT 'The key of the premium span record in MMS',
    `acct_member_sk` VARCHAR(36) NULL COMMENT 'The key of the member record in MMS',
    `premium_span_sk` VARCHAR(36) NOT NULL COMMENT 'The key of the premium span',
    `member_sk` VARCHAR(36) NOT NULL COMMENT 'The member to whom the premium span is associated',
    `exchange_member_id` VARCHAR(50) NOT NULL COMMENT 'The exchange member id of the member',
    `individual_premium_amount` DECIMAL(10,2) NOT NULL COMMENT 'The rate of the individual member',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'The date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'The date when the record was updated',
    PRIMARY KEY (`member_premium_sk`),
    INDEX `premium_span_fk_idx` (`premium_span_sk` ASC) VISIBLE,
    CONSTRAINT `premium_span_fk`
    FOREIGN KEY (`premium_span_sk`)
    REFERENCES `accountprocessordb`.`premium_span` (`premium_span_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_member_premium_member1`
    FOREIGN KEY (`member_sk`)
    REFERENCES `accountprocessordb`.`member` (`member_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'This table shows the relationship between the members and the premium spans';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`member_language` (
    `member_language_sk` VARCHAR(36) NOT NULL COMMENT 'Primary key of the table',
    `member_sk` VARCHAR(36) NOT NULL COMMENT 'The member account to which the language is associated',
    `member_acct_lang_sk` VARCHAR(36) NULL COMMENT 'The key assigned to the language record in MMS, if it is already present in MMS',
    `member_language_code` VARCHAR(50) NOT NULL COMMENT 'The unique member language code that is assigned to this record',
    `language_type_code` VARCHAR(50) NOT NULL COMMENT 'The type of language (Written, Spoken etc.)',
    `language_code` VARCHAR(50) NOT NULL COMMENT 'The ISO language code',
    `start_date` DATE NOT NULL COMMENT 'Start date of the language ',
    `end_date` DATE NULL COMMENT 'End date of the language',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'Date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'Date when the record was updated',
    PRIMARY KEY (`member_language_sk`),
    INDEX `member_language_fk_idx` (`member_sk` ASC) VISIBLE,
    CONSTRAINT `member_language_fk`
    FOREIGN KEY (`member_sk`)
    REFERENCES `accountprocessordb`.`member` (`member_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'The table contains the language for the member';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`member_identifier` (
    `member_identifier_sk` VARCHAR(36) NOT NULL COMMENT 'The primary key of the table',
    `member_acct_identifier_sk` VARCHAR(45) NULL COMMENT 'The key if the identifier is present in MMS for the member',
    `member_sk` VARCHAR(36) NOT NULL COMMENT 'The member to whom the identifier is associated',
    `member_identifier_code` VARCHAR(50) NOT NULL COMMENT 'Unique code assigned to the member identifier',
    `identifier_type_code` VARCHAR(50) NOT NULL COMMENT 'The type of identifier',
    `identifier_value` VARCHAR(50) NOT NULL COMMENT 'The value of the identifier',
    `active` BOOLEAN NOT NULL COMMENT 'Indicates if the identifier is active or not',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'The date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'The date when the record was updated',
    PRIMARY KEY (`member_identifier_sk`),
    INDEX `member_identifier_fk_idx` (`member_sk` ASC) VISIBLE,
    CONSTRAINT `member_identifier_fk`
    FOREIGN KEY (`member_sk`)
    REFERENCES `accountprocessordb`.`member` (`member_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'This table contains the member identifier records';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`member_phone` (
    `member_phone_sk` VARCHAR(36) NOT NULL COMMENT 'The primary key of the table',
    `member_acct_phone_sk` VARCHAR(45) NULL COMMENT 'The key of the phone number record from MMS',
    `member_sk` VARCHAR(36) NOT NULL COMMENT 'The member to whom the phone number is associated',
    `member_phone_code` VARCHAR(36) NOT NULL COMMENT 'The unique number for the phone number',
    `phone_type_code` VARCHAR(50) NOT NULL COMMENT 'The type of phone number',
    `phone_number` VARCHAR(50) NOT NULL COMMENT 'The phone number of the member',
    `start_date` DATE NOT NULL COMMENT 'The start date of the enrollment span',
    `end_date` DATE NULL COMMENT 'The end date of the enrollment span',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'The date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'The date when the record was created',
    PRIMARY KEY (`member_phone_sk`),
    INDEX `member_phone_sk_idx` (`member_sk` ASC) VISIBLE,
    CONSTRAINT `member_phone_sk`
    FOREIGN KEY (`member_sk`)
    REFERENCES `accountprocessordb`.`member` (`member_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'The table that contains the phone numbers';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`member_address` (
    `member_address_sk` VARCHAR(36) NOT NULL COMMENT 'The primary key of the table',
    `member_acct_address_sk` VARCHAR(36) NULL COMMENT 'The key for the address record in MMS',
    `member_sk` VARCHAR(36) NOT NULL COMMENT 'The member to whom the address is associated',
    `member_address_code` VARCHAR(50) NOT NULL COMMENT 'The unique code for the address',
    `address_type_code` VARCHAR(50) NOT NULL COMMENT 'The type of address',
    `address_line_1` VARCHAR(100) NOT NULL COMMENT 'The address line 1 of the address',
    `address_line_2` VARCHAR(50) NULL COMMENT 'The address line 2 of the address',
    `city` VARCHAR(50) NULL COMMENT 'The city of the address',
    `state_type_code` VARCHAR(50) NULL COMMENT 'The state of the address',
    `zip_code` VARCHAR(20) NULL COMMENT 'The zip code of the address',
    `county_code` VARCHAR(20) NULL COMMENT 'The county code of the address',
    `start_date` DATE NOT NULL COMMENT 'The start date of the address record',
    `end_date` DATE NULL COMMENT 'The end date of the address',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'Date when the record was updated',
    PRIMARY KEY (`member_address_sk`),
    INDEX `member_acct_address_fk_idx` (`member_sk` ASC) VISIBLE,
    CONSTRAINT `member_acct_address_fk`
    FOREIGN KEY (`member_sk`)
    REFERENCES `accountprocessordb`.`member` (`member_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'This table contains the address records of the account';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`member_email` (
    `member_email_sk` VARCHAR(36) NOT NULL COMMENT 'The primary key of the table',
    `member_sk` VARCHAR(36) NOT NULL COMMENT 'The member to whom the email is associated',
    `member_acct_email_sk` VARCHAR(36) NULL COMMENT 'The key of the email record in MMS',
    `member_email_code` VARCHAR(50) NOT NULL COMMENT 'The unique code of the email',
    `email_type_code` VARCHAR(50) NOT NULL COMMENT 'The type of the email',
    `email` VARCHAR(100) NOT NULL COMMENT 'The email of the member',
    `is_primary` BOOLEAN NOT NULL COMMENT 'Identifies if the email is the primary contact email',
    `start_date` DATE NOT NULL COMMENT 'The start date of the email record',
    `end_date` DATE NULL COMMENT 'The end date of the email',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'The date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'The date when the record was updated',
    PRIMARY KEY (`member_email_sk`),
    INDEX `member_email_fk_idx` (`member_sk` ASC) VISIBLE,
    CONSTRAINT `member_email_fk`
    FOREIGN KEY (`member_sk`)
    REFERENCES `accountprocessordb`.`member` (`member_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'This table contains the email records of the member';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`alternate_contact` (
    `alternate_contact_sk` VARCHAR(36) NOT NULL COMMENT 'The primary key of the table',
    `member_sk` VARCHAR(36) NOT NULL COMMENT 'The member to whom the alternate contact is associated',
    `acct_alt_contact_sk` VARCHAR(36) NULL COMMENT 'The key that is assigned to the alternate contact in MMS',
    `alternate_contact_code` VARCHAR(50) NOT NULL COMMENT 'Unique code assigned to the alternate contact',
    `alternate_contact_type_code` VARCHAR(50) NOT NULL COMMENT 'Identifies the type of alternate contact',
    `first_name` VARCHAR(100) NULL COMMENT 'The first name of the alternate contact',
    `middle_name` VARCHAR(50) NULL,
    `last_name` VARCHAR(100) NOT NULL COMMENT 'The last name of the alternate contact',
    `identifier_type_code` VARCHAR(50) NULL COMMENT 'The type of identifier ',
    `identifier_value` VARCHAR(50) NULL COMMENT 'The value of the identifier',
    `phone_type_code` VARCHAR(50) NULL COMMENT 'The type of phone',
    `phone_number` VARCHAR(50) NULL COMMENT 'The phone number of the alternate contact',
    `email` VARCHAR(50) NULL COMMENT '  The email of the alternate contact',
    `address_line_1` VARCHAR(100) NULL COMMENT 'The address line 1 of the address',
    `address_line_2` VARCHAR(50) NULL COMMENT 'The address line 2 of the address',
    `city` VARCHAR(50) NULL COMMENT 'The city of the address ',
    `state_type_code` VARCHAR(50) NULL COMMENT 'The state of the address',
    `zip_code` VARCHAR(50) NULL COMMENT 'The zip code of the address',
    `start_date` DATE NOT NULL COMMENT 'The start date of the alternate contact',
    `end_date` DATE NULL COMMENT 'The end date of the alternate contact',
    `changed` BOOLEAN NOT NULL COMMENT 'Indicates if entity was updated by the transaction',
    `created_date` DATETIME NULL COMMENT 'The date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'The date when the record was updated',
    PRIMARY KEY (`alternate_contact_sk`),
    INDEX `alt_contact_fk_idx` (`member_sk` ASC) VISIBLE,
    CONSTRAINT `alt_contact_fk`
    FOREIGN KEY (`member_sk`)
    REFERENCES `accountprocessordb`.`member` (`member_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'This table contains the alternate contacts associated with the member';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`payload_tracker` (
    `payload_tracker_sk` VARCHAR(36) NOT NULL,
    `payload_id` VARCHAR(45) NOT NULL COMMENT 'A unique id assigned for the payload',
    `payload_key` VARCHAR(50) NOT NULL COMMENT 'The key for the type of payload, like account number for account payload and zeus transaction control number for transaction payload.',
    `payload_key_type_code` VARCHAR(45) NOT NULL COMMENT 'Identifies the type of payload like ACCOUNT, TRANSACTION, FILE etc',
    `payload` LONGTEXT NOT NULL COMMENT 'The payload as a string',
    `payload_direction_type_code` VARCHAR(45) NOT NULL COMMENT 'Identifies the direction of the payload INBOUND or OUTBOUND',
    `src_dest` VARCHAR(100) NOT NULL COMMENT 'Identifies the source if the payload is inbound and destination if the payload is outbound',
    `created_date` DATETIME NULL COMMENT 'Date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'Date when the record was updated',
    PRIMARY KEY (`payload_tracker_sk`))
    ENGINE = InnoDB
    COMMENT = 'This table contains all the payloads that are sent out or received in to the transaction storage service';
CREATE TABLE IF NOT EXISTS `accountprocessordb`.`payload_tracker_detail` (
    `payload_tracker_detail_sk` VARCHAR(36) NOT NULL,
    `payload_tracker_sk` VARCHAR(36) NOT NULL COMMENT 'The foreign key of the payload tracker table',
    `response_type_code` VARCHAR(45) NOT NULL COMMENT 'The type of response received or sent. e.g. ACK, RESULT etc',
    `response_payload_id` VARCHAR(45) NOT NULL COMMENT 'The unique id assigned to the response payload',
    `response_payload` LONGTEXT NOT NULL,
    `payload_direction_type_code` VARCHAR(45) NOT NULL COMMENT 'Identifies the direction of the payload INBOUND or OUTBOUND',
    `src_dest` VARCHAR(100) NOT NULL COMMENT 'Identifies the source if the payload is inbound and destination if the payload is outbound',
    `created_date` DATETIME NULL COMMENT 'Date when the record was created',
    `updated_date` DATETIME NULL COMMENT 'Date when the record was updated',
    PRIMARY KEY (`payload_tracker_detail_sk`),
    INDEX `payload_tracker_fk_idx` (`payload_tracker_sk` ASC) VISIBLE,
    CONSTRAINT `payload_tracker_fk`
    FOREIGN KEY (`payload_tracker_sk`)
    REFERENCES `accountprocessordb`.`payload_tracker` (`payload_tracker_sk`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
    ENGINE = InnoDB
    COMMENT = 'The payload tracker detail table, that tracks all the responses received for an outbound payload and all the responses sent for an inbound payload';