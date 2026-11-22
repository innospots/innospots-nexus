import { platformPublicFetch } from "./platform-public-api";

/** 与后端 {@code RegistrationMode} 一致。 */
export type RegistrationMode = "OPEN" | "INVITE" | "APPROVAL" | "CLOSED";

/** 与后端 {@code RegistrationOtpVerification} 一致。 */
export type RegistrationOtpVerification = "EMAIL" | "MOBILE" | "NONE";

/** GET /api/d/platform/public/registration/policy 响应体。 */
export type PublicRegistrationPolicy = {
    registrationMode: RegistrationMode;
    otpVerification: RegistrationOtpVerification;
    openRegistrationEnabled: boolean;
    inviteRegistrationEnabled: boolean;
    approvalRegistrationEnabled: boolean;
};

export async function fetchPublicRegistrationPolicy(): Promise<PublicRegistrationPolicy> {
    return platformPublicFetch<PublicRegistrationPolicy>("/public/registration/policy");
}
