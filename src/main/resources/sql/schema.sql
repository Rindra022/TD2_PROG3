create type category_type as enum('VEGETABLE','ANIMAL','MARINE','DAIRY','OTHER');
create type dish_types as enum ('START', 'MAIN', 'DESSERT');


create table dish
(
    id serial primary key,
    name varchar(150) not null,
    dish_type dish_types
);

create table ingredient
(
    id serial primary key,
    name varchar(150) not null,
    price numeric(10,2) not null,
    category category_type,
    id_dish integer not null,
    constraint fk_dish
        foreign key(id_dish) references dish(id)
);
