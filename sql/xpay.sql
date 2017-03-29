CREATE TABLE IF NOT EXISTS bill_app (
	id BIGSERIAL PRIMARY KEY,
	app_key varchar(64) NOT NULL,
	app_secret varchar(256) NOT NULL,
	create_date TIMESTAMP WITH TIME ZONE NOT NULL default now(), 
	update_date TIMESTAMP WITH TIME ZONE NOT NULL default now(),
	deleted boolean DEFAULT FALSE
);	
ALTER SEQUENCE bill_app_id_seq RESTART 10;
CREATE INDEX idx_app_key ON bill_app(app_key); 

CREATE TABLE IF NOT EXISTS bill_store (
	id BIGSERIAL PRIMARY KEY,
	code varchar(32) NOT NULL,
	name varchar(64) NOT NULL,
	create_date TIMESTAMP WITH TIME ZONE NOT NULL default now(), 
	update_date TIMESTAMP WITH TIME ZONE NOT NULL default now(),
	deleted boolean DEFAULT FALSE
);
ALTER SEQUENCE bill_store_id_seq RESTART 100;
CREATE INDEX idx_bill_store_code ON bill_store(code); 

CREATE TABLE IF NOT EXISTS bill_store_channel (
	id BIGSERIAL PRIMARY KEY,
	store_id BIGINT NOT NULL,
	ext_store_id varchar(32) NOT NULL,
	payment_gateway varchar(16) NOT NULL,
	create_date TIMESTAMP WITH TIME ZONE NOT NULL default now(), 
	update_date TIMESTAMP WITH TIME ZONE NOT NULL default now(),
	deleted boolean DEFAULT FALSE
);
ALTER SEQUENCE bill_store_channel_id_seq RESTART 100;
CREATE INDEX idx_bill_store_channel ON bill_store_channel(store_id); 

CREATE TABLE IF NOT EXISTS bill_order (
	id BIGSERIAL PRIMARY KEY,
	order_no varchar(32) NOT NULL,
	app_id BIGINT NOT NULL,
	store_id BIGINT NOT NULL,
	store_channel BIGINT NOT NULL,
	total_fee varchar(10) NOT NULL,
	order_time varchar(14) NOT NULL,
	pay_channel INT NOT NULL,
	seller_order_no varchar(64),
	ext_order_no varchar(64),
	attach varchar(256),
	device_id varchar(32),
	ip varchar(32),
	notify_url varchar(256),
	code_url varchar(256),
	prepay_id varchar(64),
	status varchar(16),
	create_date TIMESTAMP WITH TIME ZONE NOT NULL default now(), 
	update_date TIMESTAMP WITH TIME ZONE NOT NULL default now(),
	deleted boolean DEFAULT FALSE
);
ALTER SEQUENCE bill_order_id_seq RESTART 1000;
CREATE INDEX idx_order_no ON bill_order(order_no); 

CREATE TABLE IF NOT EXISTS bill_order_detail (
	order_id BIGINT NOT NULL,
	store_name varchar(64),
	operator varchar(64),
	subject varchar(64),
	description varchar(256),
	items varchar(2048),
	create_date TIMESTAMP WITH TIME ZONE NOT NULL default now(), 
	update_date TIMESTAMP WITH TIME ZONE NOT NULL default now(),
	deleted boolean DEFAULT FALSE
);
CREATE INDEX idx_order_detial_id ON bill_order_detail(order_id); 