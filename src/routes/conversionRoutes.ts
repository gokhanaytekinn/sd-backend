import { Router } from 'express';
import * as conversionController from '../controllers/conversionController';
import { authenticate } from '../middleware/auth';

const router = Router();

router.use(authenticate);

router.post('/upgrade', conversionController.convertToPremium);
router.post('/downgrade', conversionController.downgradeToFree);

export default router;
