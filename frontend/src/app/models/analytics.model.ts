export type EndpointAnalytics = {
	uri: string,
	count: number
}

export type APIAnalytics = {
	timeStamp: string,
	totalRequests: number,
	totalErrors: number,
	totalSuccesses: number,
	averageResponseTime: number
	mostDemandedEndpoints: EndpointAnalytics[]
}

export type TimeRange = '1h' | '1d' | '1w' | '1m';

export type AnalyticsCards = {
	iconName: string,
	iconStyles: string,
	label: string,
	textStyles: string,
	value: number,
	isRate: boolean,
}