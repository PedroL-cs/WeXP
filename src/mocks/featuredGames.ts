import type { Game } from '../types/Game';

export const featuredGames: {
  topSellers: Game[];
  specials: Game[];
  newReleases: Game[];
} = {
  topSellers: [
    {
      id: 2584270,
      name: 'Mortal Shell II',
      discountPercent: 0,
      originalPrice: 15900,
      finalPrice: 15900,
      achievements: 18,
      largeCapsuleImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2584270/5b6a6b59c4f857165987b15fc116e5b3a06427dc/capsule_616x353.jpg',
      headerImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2584270/0a4c23b70ced29fac344186ea564dae67c80cfd6/header.jpg',
    },

    {
      id: 2358720,
      name: 'Black Myth: Wukong',
      discountPercent: 30,
      originalPrice: 22999,
      finalPrice: 16099,
      achievements: 44,
      largeCapsuleImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2358720/f40ef565c380c617020e559b4b4b089edd93ec09/capsule_616x353.jpg',
      headerImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2358720/header.jpg',
    },

    {
      id: 227300,
      name: 'Euro Truck Simulator 2',
      discountPercent: 75,
      originalPrice: 6199,
      finalPrice: 1549,
      achievements: 132,
      largeCapsuleImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/227300/5237101dc9efe4873c9be1804d4ed2619ad7bee4/capsule_616x353.jpg?t=1785394779',
      headerImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/227300/9a81dc3126c56637297b654f9dcac057cfd79b77/header.jpg?t=1785394779',
    },

    {
      id: 4435490,
      name: 'Call of Duty®: Modern Warfare® 4',
      discountPercent: 0,
      originalPrice: 29900,
      finalPrice: 29900,
      achievements: 32,
      largeCapsuleImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4435490/82fb9a3dce1b813018f55007749323d379b78c03/capsule_616x353_alt_assets_0_brazilian.jpg',
      headerImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4435490/0c492be3204835d71e8b454b6eb1817f8ff68c0d/header.jpg',
    },
  ],

  specials: [
    {
      id: 2358720,
      name: 'Black Myth: Wukong',
      discountPercent: 30,
      originalPrice: 22999,
      finalPrice: 16099,
      achievements: 39,
      largeCapsuleImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2358720/f40ef565c380c617020e559b4b4b089edd93ec09/capsule_616x353.jpg',
      headerImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2358720/header.jpg',
    },

    {
      id: 694280,
      name: 'Zombie Army 4: Dead War',
      discountPercent: 95,
      originalPrice: 13499,
      finalPrice: 674,
      achievements: 25,
      largeCapsuleImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/694280/capsule_616x353.jpg',
      headerImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/694280/header.jpg',
    },
  ],

  newReleases: [
    {
      id: 4945260,
      name: 'TMR simulator',
      discountPercent: 0,
      originalPrice: null,
      finalPrice: 0,
      achievements: 32,
      largeCapsuleImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4945260/31db80c1febd1ffcf868128d297d6b02ec82f1ff/capsule_616x353.jpg',
      headerImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4945260/d3cac8674060e48affb658b06c68a5f7bf3531b2/header.jpg',
    },

    {
      id: 4586750,
      name: 'Nova Fleet',
      discountPercent: 15,
      originalPrice: 1185,
      finalPrice: 1007,
      achievements: 15,
      largeCapsuleImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4586750/2c699143aab648f516d164a21976737187a9698d/capsule_616x353.jpg',
      headerImage:
        'https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4586750/0331e7038af70bab6a27426b217e1747c1ca3ac7/header.jpg',
    },
  ],
};
