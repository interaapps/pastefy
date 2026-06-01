export type AnalyticsSeriesPoint = {
  bucket: string
  visits: number
  unique_visitors: number
}

export type AnalyticsBreakdownPoint = {
  value: string
  visits: number
  unique_visitors: number
}

export type AnalyticsResponse = {
  total_visits: number
  unique_visitors: number
  bot_visits: number
  bot_tracking_enabled: boolean
  series: AnalyticsSeriesPoint[]
  breakdown: AnalyticsBreakdownPoint[]
}
