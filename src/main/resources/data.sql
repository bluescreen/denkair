-- Seed data. Dates anchored to relative DATEADD so the demo always shows future flights.

INSERT INTO airport (iata, name, city, country, image_url) VALUES
 ('HAM', 'Hamburg Airport',       'Hamburg',      'DE', 'https://picsum.photos/seed/ham/800/600'),
 ('DUS', 'Düsseldorf Airport',    'Düsseldorf',   'DE', 'https://picsum.photos/seed/dus/800/600'),
 ('FRA', 'Frankfurt Main',        'Frankfurt',    'DE', 'https://picsum.photos/seed/fra/800/600'),
 ('PMI', 'Palma de Mallorca',     'Palma',        'ES', 'https://picsum.photos/seed/pmi/800/600'),
 ('AYT', 'Antalya',               'Antalya',      'TR', 'https://picsum.photos/seed/ayt/800/600'),
 ('LPA', 'Gran Canaria',          'Las Palmas',   'ES', 'https://picsum.photos/seed/lpa/800/600'),
 ('HER', 'Heraklion',             'Kreta',        'GR', 'https://picsum.photos/seed/her/800/600'),
 ('HRG', 'Hurghada',              'Hurghada',     'EG', 'https://picsum.photos/seed/hrg/800/600'),
 ('FAO', 'Faro',                  'Faro',         'PT', 'https://picsum.photos/seed/fao/800/600');

INSERT INTO aircraft (type_code, seats, registration) VALUES
 ('A320-200', 180, 'D-HANB'),
 ('A321neo',  220, 'D-HANF'),
 ('B757-300', 275, 'D-HANR');

INSERT INTO flight (flight_number, origin_id, destination_id, aircraft_id, departure, arrival, preis, seats_available, image_url, aktiv) VALUES
 ('HA4021', 1, 4, 2, DATEADD('DAY', 14, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, DATEADD('DAY', 14, CURRENT_TIMESTAMP)), 189.00, 180, 'https://picsum.photos/seed/ha4021/1200/600', TRUE),
 ('HA4022', 4, 1, 2, DATEADD('DAY', 21, CURRENT_TIMESTAMP), DATEADD('HOUR', 2, DATEADD('DAY', 21, CURRENT_TIMESTAMP)), 169.00, 180, 'https://picsum.photos/seed/ha4022/1200/600', TRUE),
 ('HA4108', 2, 5, 1, DATEADD('DAY', 7,  CURRENT_TIMESTAMP), DATEADD('HOUR', 4, DATEADD('DAY', 7,  CURRENT_TIMESTAMP)), 239.00, 160, 'https://picsum.photos/seed/ha4108/1200/600', TRUE),
 ('HA4109', 5, 2, 1, DATEADD('DAY', 14, CURRENT_TIMESTAMP), DATEADD('HOUR', 4, DATEADD('DAY', 14, CURRENT_TIMESTAMP)), 229.00, 160, 'https://picsum.photos/seed/ha4109/1200/600', TRUE),
 ('HA4310', 3, 6, 3, DATEADD('DAY', 10, CURRENT_TIMESTAMP), DATEADD('HOUR', 5, DATEADD('DAY', 10, CURRENT_TIMESTAMP)), 299.00, 250, 'https://picsum.photos/seed/ha4310/1200/600', TRUE),
 ('HA4311', 6, 3, 3, DATEADD('DAY', 17, CURRENT_TIMESTAMP), DATEADD('HOUR', 5, DATEADD('DAY', 17, CURRENT_TIMESTAMP)), 279.00, 250, 'https://picsum.photos/seed/ha4311/1200/600', TRUE),
 ('HA4501', 1, 7, 2, DATEADD('DAY', 12, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 12, CURRENT_TIMESTAMP)), 209.00, 200, 'https://picsum.photos/seed/ha4501/1200/600', TRUE),
 ('HA4502', 7, 1, 2, DATEADD('DAY', 19, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 19, CURRENT_TIMESTAMP)), 199.00, 200, 'https://picsum.photos/seed/ha4502/1200/600', TRUE),
 ('HA4707', 2, 8, 3, DATEADD('DAY', 21, CURRENT_TIMESTAMP), DATEADD('HOUR', 5, DATEADD('DAY', 21, CURRENT_TIMESTAMP)), 319.00, 260, 'https://picsum.photos/seed/ha4707/1200/600', TRUE),
 ('HA4801', 3, 9, 1, DATEADD('DAY', 9,  CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 9,  CURRENT_TIMESTAMP)), 179.00, 160, 'https://picsum.photos/seed/ha4801/1200/600', TRUE);
