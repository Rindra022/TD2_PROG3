insert into stock_movement (id_ingredient, quantity, unit, creation_datetime, type)
values (1, 5.0, 'KG', '2024-01-05 08:00', 'IN'),
       (1, 0.2, 'KG', '2024-01-06 12:00', 'OUT'),
       (2, 4.0, 'KG', '2024-01-05 08:00', 'IN'),
       (2, 0.15, 'KG', '2024-01-06 12:00', 'OUT'),
       (3, 10.0, 'KG', '2024-01-04 09:00', 'IN'),
       (3, 1.0, 'KG', '2024-01-06 13:00', 'OUT'),
       (4, 3.0, 'KG', '2024-01-05 10:00', 'IN'),
       (4, 0.3, 'KG', '2024-01-06 14:00', 'OUT'),
       (5, 2.5, 'KG', '2024-01-05 10:00', 'IN'),
       (5, 0.2, 'KG', '2024-01-06 14:00', 'OUT');

select setval('stock_movement_id_seq', (select max(id) from stock_movement));