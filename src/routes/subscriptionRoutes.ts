import { Router } from 'express';
import * as subscriptionController from '../controllers/subscriptionController';
import { authenticate } from '../middleware/auth';

const router = Router();

router.use(authenticate);

router.get('/', subscriptionController.getSubscriptions);
router.get('/suspicious', subscriptionController.getSuspiciousSubscriptions);
router.get('/:id', subscriptionController.getSubscription);
router.post('/', subscriptionController.createSubscription);
router.patch('/:id/cancel', subscriptionController.cancelSubscription);
router.patch('/:id/flag', subscriptionController.flagSubscriptionAsSuspicious);
router.patch('/:id/approve', subscriptionController.approveSubscription);

export default router;
