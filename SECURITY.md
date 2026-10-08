# Sécurité

## Signaler une vulnérabilité

Ne publiez pas de vulnérabilité, secret ou donnée personnelle dans une issue publique. Contactez les mainteneurs par un canal privé approuvé. Si le signalement privé de vulnérabilités GitHub est activé pour ce dépôt, utilisez-le.

Merci de fournir le composant concerné, les étapes permettant de reproduire le problème et son impact. N'incluez pas de données réelles d'employés.

## Gestion des secrets

- Ne commitez aucun fichier `.env` réel, mot de passe, clé JWT ou jeton.
- Stockez les secrets de développement dans l'environnement local ; stockez ceux des déploiements dans le gestionnaire de secrets de la plateforme.
- Si un secret est exposé, révoquez-le et faites-le tourner immédiatement. Supprimer la valeur du dernier commit ne suffit pas à l'effacer de l'historique.
